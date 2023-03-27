package com.lin.opush.service.impl;


import com.lin.opush.service.RecallService;
import com.lin.opush.domain.SendRequest;
import com.lin.opush.domain.SendResponse;
import com.lin.opush.chain.ExecutionChainContext;
import com.lin.opush.chain.ExecutionController;
import com.lin.opush.domain.SendTaskModel;
import com.lin.opush.vo.BasicResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消息撤回接口实现
 */
@Service
public class RecallServiceImpl implements RecallService {

    @Autowired
    private ExecutionController executionController;

    @Override
    public SendResponse recall(SendRequest sendRequest) {
        // 组装发送消息任务模型
        SendTaskModel sendTaskModel = SendTaskModel.builder()
                                        .messageTemplateId(sendRequest.getMessageTemplateId())
                                        .build();
        // 组装执行链上下文
        ExecutionChainContext context = ExecutionChainContext.builder()
                                            .code(sendRequest.getCode())
                                            .executionChainDataModel(sendTaskModel)
                                            .needBreak(false)
                                            .response(BasicResultVO.success()).build();
        // 执行执行链
        context = executionController.execute(context);
        // 返回执行链执行结果
        return new SendResponse(context.getResponse().getStatus(), context.getResponse().getMsg());
    }
}
