package com.lin.opush.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.util.StrUtil;
import com.lin.opush.csv.CountCsvFileRowNumberHandler;
import com.lin.opush.dao.MessageTemplateDao;
import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.delay.AbstractDelayProcess;
import com.lin.opush.delay.DelayToBatchSend;
import com.lin.opush.service.ProcessTimedJob;
import com.lin.opush.utils.ReadFileUtils;
import com.lin.opush.domain.SingleRowInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * 处理定时任务实现类
 */
@Service
@Slf4j
public class ProcessTimedJobImpl implements ProcessTimedJob {
    /**
     * 消息模板 Dao
     */
    @Autowired
    private MessageTemplateDao messageTemplateDao;

    /**
     * 应用上下文
     */
    @Autowired
    private ApplicationContext context;

    /**
     * 处理定时任务
     * @param creator 消息模板创建者
     * @param messageTemplateId 消息模板id
     */
    @Override
    public void process(String creator, Long messageTemplateId) {
        // 根据模板id获取模板信息
        MessageTemplate messageTemplate = messageTemplateDao.findById(messageTemplateId).orElse(null);
        if (Objects.isNull(messageTemplate)) {
            return;
        }
        // 定时发送的人群文件路径是否为空
        if (StrUtil.isBlank(messageTemplate.getTimedSendCrowdFilePath())) {
            log.error("ProcessTimedJob#process timedSendCrowdFilePath empty! messageTemplateId:{}", messageTemplateId);
            return;
        }
        // 统计并返回Csv文件数据行数
        long csvFileRowNumber = ReadFileUtils.countCsvFileRowNumber(messageTemplate.getTimedSendCrowdFilePath(),
                                                                        new CountCsvFileRowNumberHandler());
        // 读取文件每一行记录并存入阻塞队列做延迟处理【批量发送】
        DelayToBatchSend delayToBatchSend = context.getBean(DelayToBatchSend.class);
        ReadFileUtils.getCsvRow(messageTemplate.getTimedSendCrowdFilePath(), csvRow -> {
            // csvRow.getFieldMap()将行对象转为key为标题，value为标题对应值
            // 行对象转换的Map内容不为空且Map中接收者id【userId】不为空
            if (CollUtil.isEmpty(csvRow.getFieldMap()) ||
                    StrUtil.isBlank(csvRow.getFieldMap().get(ReadFileUtils.RECEIVER_KEY))) {
                return;
            }
            // 每一行记录存入阻塞队列做延迟处理【批量发送】
            Map<String, String> params = ReadFileUtils.getParamsFromCsvRow(csvRow.getFieldMap());
            SingleRowInfo singleRowInfo = SingleRowInfo.builder()
                                                    .messageTemplateId(messageTemplateId)
                                                    .receiver(csvRow.getFieldMap().get(ReadFileUtils.RECEIVER_KEY))
                                                    .params(params).creator(creator).build();
            delayToBatchSend.put(singleRowInfo);
            // 判断是否已读取完文件，读取完则回收资源且更改状态
            isComplete(csvRow, csvFileRowNumber, delayToBatchSend, messageTemplateId);
        });
    }

    /**
     * 判断是否完成Csv文件读取
     * 【完成则暂停单线程池消费(最后会回收线程池资源)，更改消息模板的状态(暂未实现)】
     * @param csvRow Csv文件行对象
     * @param csvFileRowNumber csv文件行数
     * @param delayToBatchSend 延迟处理类
     * @param messageTemplateId 消息模板id
     */
    private void isComplete(CsvRow csvRow, long csvFileRowNumber, AbstractDelayProcess delayToBatchSend, Long messageTemplateId) {
        // 将csvRow的原始行号与csv文件行数对比，判断是否已处理完csv文件
        if (csvRow.getOriginalLineNumber() == csvFileRowNumber) {
            delayToBatchSend.setStop(true);
            log.info("messageTemplate:[{}] read csv file complete!", messageTemplateId);
        }
    }
}
