package com.lin.opush.service.impl;

import cn.monitor4all.logRecord.annotation.OperationLog;
import com.lin.opush.service.SendService;
import com.lin.opush.domain.BatchSendRequest;
import com.lin.opush.domain.SendRequest;
import com.lin.opush.domain.SendResponse;
import com.lin.opush.chain.ExecutionChainContext;
import com.lin.opush.chain.ExecutionController;
import com.lin.opush.domain.SendTaskModel;
import com.lin.opush.vo.BasicResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 单条/批量消息发送接口实现
 */
@Service
public class SendServiceImpl implements SendService {
    @Autowired
    private ExecutionController executionController;

    @Override
    @OperationLog(bizType = "SendService#send", bizId = "#sendRequest.messageTemplateId", msg = "#sendRequest")
    public SendResponse send(SendRequest sendRequest) {
        // 组装发送消息任务模型
        SendTaskModel sendTaskModel = SendTaskModel.builder()
                                        .messageTemplateId(sendRequest.getMessageTemplateId())
                                        .messageParamList(Collections.singletonList(sendRequest.getMessageParam()))
                                        .creator(sendRequest.getCreator()).build();
        // 组装执行链上下文
        ExecutionChainContext context = ExecutionChainContext.builder()
                                            .code(sendRequest.getCode())
                                            .executionChainDataModel(sendTaskModel)
                                            .needBreak(false)
                                            .response(BasicResultVO.success()).build();
        // 执行执行链
        ExecutionChainContext process = executionController.execute(context);
        // 返回执行链执行结果
        return new SendResponse(process.getResponse().getStatus(), process.getResponse().getMsg());
    }

    @Override
    @OperationLog(bizType = "SendService#batchSend", bizId = "#batchSendRequest.messageTemplateId", msg = "#batchSendRequest")
    public SendResponse batchSend(BatchSendRequest batchSendRequest) {
        // 组装发送消息任务模型
        SendTaskModel sendTaskModel = SendTaskModel.builder()
                                        .messageTemplateId(batchSendRequest.getMessageTemplateId())
                                        .messageParamList(batchSendRequest.getMessageParamList())
                                        .creator(batchSendRequest.getCreator()).build();
        // 组装执行链上下文
        ExecutionChainContext context = ExecutionChainContext.builder()
                                            .code(batchSendRequest.getCode())
                                            .executionChainDataModel(sendTaskModel)
                                            .needBreak(false)
                                            .response(BasicResultVO.success()).build();
        // 执行执行链
        context = executionController.execute(context);
        // 返回执行链执行结果
        return new SendResponse(context.getResponse().getStatus(), context.getResponse().getMsg());
    }
}
