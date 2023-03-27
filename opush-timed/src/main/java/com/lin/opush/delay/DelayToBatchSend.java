package com.lin.opush.delay;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.google.common.collect.Lists;
import com.lin.opush.config.TimedThreadPoolConfig;
import com.lin.opush.constants.CommonConstant;
import com.lin.opush.constants.DelayProcessConstant;
import com.lin.opush.domain.BatchSendRequest;
import com.lin.opush.domain.MessageParam;
import com.lin.opush.enums.BusinessCode;
import com.lin.opush.service.SendService;
import com.lin.opush.domain.SingleRowInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 延迟消息发送实现批量发送
 * 【当达到数量阈值/时间阈值时调用批量发送接口进行消息推送】
 */
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DelayToBatchSend extends AbstractDelayProcess<SingleRowInfo> {
    /**
     * 发送服务
     */
    @Autowired
    private SendService sendService;

    /**
     * 初始化延迟处理参数
     */
    public DelayToBatchSend() {
        DelayProcessParam<SingleRowInfo> delayProcessParam = new DelayProcessParam<>();
        // 阻塞队列大小100、数量阈值100、时间阈值1s
        delayProcessParam.setQueue(new LinkedBlockingQueue(DelayProcessConstant.QUEUE_SIZE))
                        .setNumberThreshold(DelayProcessConstant.NUMBER_THRESHOLD)
                        .setTimeThreshold(DelayProcessConstant.TIME_THRESHOLD)
                        .setExecutorService(TimedThreadPoolConfig.getConsumeBlockingQueueThreadPool());
        this.delayProcessParam = delayProcessParam;
    }

    /**
     * 真正消费阻塞队列中元素【单行信息组成缓存行信息】→ 批量发送
     * @param rowInfos 单行信息组成缓存行信息列表
     */
    @Override
    public void realConsume(List<SingleRowInfo> rowInfos) {
        // 若消息内容中的可变部分【params】相同，则组装成同一个MessageParam发送
        Map<Map<String, String>, String> paramsReceiverMap = MapUtil.newHashMap();
        for (SingleRowInfo singleRowInfo : rowInfos) {
            String receiver = singleRowInfo.getReceiver();
            Map<String, String> params = singleRowInfo.getParams();
            if (Objects.isNull(paramsReceiverMap.get(params))) {
                paramsReceiverMap.put(params, receiver);
            } else {
                String newReceiver = StringUtils.join(
                        new String[]{paramsReceiverMap.get(params), receiver}, CommonConstant.COMMA);
                paramsReceiverMap.put(params, newReceiver);
            }
        }
        // 组装参数
        List<MessageParam> messageParams = Lists.newArrayList();
        for (Map.Entry<Map<String, String>, String> entry : paramsReceiverMap.entrySet()) {
            MessageParam messageParam = MessageParam.builder().receiver(entry.getValue())
                                                    .variables(entry.getKey()).build();
            messageParams.add(messageParam);
        }
        // 调用批量发送接口发送消息
        BatchSendRequest batchSendRequest = BatchSendRequest.builder().code(BusinessCode.SEND.getCode())
                .messageParamList(messageParams)
                .messageTemplateId(CollUtil.getFirst(rowInfos.iterator()).getMessageTemplateId())
                .creator(rowInfos.get(0).getCreator()).build();
        sendService.batchSend(batchSendRequest);
    }
}
