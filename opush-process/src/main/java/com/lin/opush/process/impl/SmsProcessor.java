package com.lin.opush.process.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.lin.opush.constants.CommonConstant;
import com.lin.opush.dao.SmsRecordDao;
import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.domain.SmsRecord;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.domain.sms.MessageTypeConfig;
import com.lin.opush.domain.sms.SmsParam;
import com.lin.opush.dto.model.SmsContentModel;
import com.lin.opush.enums.ChannelType;
import com.lin.opush.process.BaseProcessor;
import com.lin.opush.process.Processor;
import com.lin.opush.script.SmsScript;
import com.lin.opush.utils.LoadBalanceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 短信处理器
 */
@Component
@Slf4j
public class SmsProcessor extends BaseProcessor implements Processor {
    /**
     * 处理器初始化（渠道类型编码）
     */
    public SmsProcessor() {
        channelTypeCode = ChannelType.SMS.getCode();
    }

    /**
     * 流量负载工具类
     */
    @Autowired
    private LoadBalanceUtils loadBalanceUtils;

    /**
     * 短信记录 Dao
     */
    @Autowired
    private SmsRecordDao smsRecordDao;

    /**
     * 自动注入SmsScript的实现类对象【TencentSmsScript...】到Map<实现类对象名,实现类对象>
     * 【只有在Map的key为String类型时才有效】
     */
    @Autowired
    private Map<String, SmsScript> smsScripts;

    /**
     * 若短信含链接，则将链接拼在文案之后
     *      (1)可以考虑将链接转短链
     *      (2)若为营销类短信，需考虑拼接【回TD退订】之类的文案
     * @param taskInfo 任务信息
     * @return 短信内容
     */
    private String getSmsContent(TaskInfo taskInfo) {
        SmsContentModel smsContentModel = (SmsContentModel) taskInfo.getContentModel();
        if (StrUtil.isNotBlank(smsContentModel.getUrl())) {
            // 拼接链接
            return smsContentModel.getContent() + CommonConstant.SPACE + smsContentModel.getUrl();
        } else {
            return smsContentModel.getContent();
        }
    }

    /**
     * 发送短信
     * @param taskInfo 任务信息
     * @return 发送是否成功
     */
    @Override
    public boolean realSend(TaskInfo taskInfo) {
        // 组装发送短信参数【发送手机号、发送内容、消息模板Id、下发者】
        SmsParam smsParam = SmsParam.builder().phones(taskInfo.getReceiver())
                                                .content(getSmsContent(taskInfo))
                                                .messageTemplateId(taskInfo.getMessageTemplateId())
                                                .creator(taskInfo.getCreator()).build();
        try {
            // 获取不同消息类型的流量配置进行流量负载并发送短信
            MessageTypeConfig[] messageTypeConfigs = loadBalanceUtils.loadBalance(loadBalanceUtils.getMessageTypeConfig(taskInfo));
            for (MessageTypeConfig messageTypeConfig : messageTypeConfigs) {
                smsParam.setScriptName(messageTypeConfig.getScriptName());
                smsParam.setSendAccountId(messageTypeConfig.getSendAccount());
                // 短信下发记录列表
                List<SmsRecord> recordList = smsScripts.get(messageTypeConfig.getScriptName()).send(smsParam);
                if (CollUtil.isNotEmpty(recordList)) {
                    // 短信记录持久化
                    smsRecordDao.saveAll(recordList);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("SmsProcessor#realSend fail:{},params:{}", Throwables.getStackTraceAsString(e), JSON.toJSONString(smsParam));
        }
        return false;
    }

    /**
     * 撤回短信
     * @param messageTemplate 消息模板
     */
    @Override
    public void recall(MessageTemplate messageTemplate) {}
}
