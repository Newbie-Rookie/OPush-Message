package com.lin.opush.action;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Throwables;

import com.lin.opush.enums.BusinessCode;
import com.lin.opush.enums.RespStatusEnum;
import com.lin.opush.mq.SendMqService;
import com.lin.opush.chain.ExecutionAction;
import com.lin.opush.chain.ExecutionChainContext;
import com.lin.opush.domain.SendTaskModel;
import com.lin.opush.vo.BasicResultVO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 将消息发送到MQ
 */
@Slf4j
@Service
public class SendMqAction implements ExecutionAction<SendTaskModel> {
    @Autowired
    private SendMqService sendMqService;

    @Value("${opush.business.send.topic.name}")
    private String sendMessageTopic;

    @Value("${opush.business.recall.topic.name}")
    private String recallMessageTopic;

    @Override
    public void execute(ExecutionChainContext<SendTaskModel> context) {
        // 发送任务数据模型
        SendTaskModel sendTaskModel = context.getExecutionChainDataModel();
        try {
            // 根据不同消息业务做不同处理
            if (BusinessCode.SEND.getCode().equals(context.getCode())) {
                // 将发送任务列表转为JSON字符串，并将该JSON字符串对应的对象类型（TaskInfo）作为type参数纳入JSON字符串中
                String message = JSON.toJSONString(sendTaskModel.getTaskInfo(),
                                                    new SerializerFeature[]{SerializerFeature.WriteClassName});
                sendMqService.send(sendMessageTopic, message);
            } else if (BusinessCode.RECALL.getCode().equals(context.getCode())) {
                // 将发送任务列表转为JSON字符串，并将该JSON字符串对应的对象类型（MessageTemplate）作为type参数纳入JSON字符串中
                String message = JSON.toJSONString(sendTaskModel.getMessageTemplate(),
                                                    new SerializerFeature[]{SerializerFeature.WriteClassName});
                sendMqService.send(recallMessageTopic, message);
            }
        } catch (Exception e) {
            context.setNeedBreak(true).setResponse(BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR));
            log.error("send {} fail! e:{},params:{}", Throwables.getStackTraceAsString(e),
                    JSON.toJSONString(CollUtil.getFirst(sendTaskModel.getTaskInfo().listIterator())));
        }
    }
}
