package com.lin.opush.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.lin.opush.constants.OpushConstant;
import com.lin.opush.domain.MessageParam;
import com.lin.opush.enums.RespStatusEnum;
import com.lin.opush.chain.ExecutionAction;
import com.lin.opush.chain.ExecutionChainContext;
import com.lin.opush.domain.SendTaskModel;
import com.lin.opush.vo.BasicResultVO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 前置参数校验
 */
@Slf4j
@Service
public class PreParamCheckAction implements ExecutionAction<SendTaskModel> {
    @Override
    public void execute(ExecutionChainContext<SendTaskModel> context) {
        // 执行链上下文的数据模型（发送任务模型）
        SendTaskModel sendTaskModel = context.getExecutionChainDataModel();
        // 1、创建者不能为空或空字符串
        if (StrUtil.isBlank(sendTaskModel.getCreator())) {
            context.setNeedBreak(true).setResponse(BasicResultVO.fail(RespStatusEnum.NO_LOGIN));
            return;
        }
        // 模板id
        Long messageTemplateId = sendTaskModel.getMessageTemplateId();
        // 模板请求参数列表
        List<MessageParam> messageParamList = sendTaskModel.getMessageParamList();
        // 2、未传入消息模板Id / 请求参数列表则报客户端参数错误
        if (Objects.isNull(messageTemplateId) || CollUtil.isEmpty(messageParamList)) {
            context.setNeedBreak(true).setResponse(BasicResultVO.fail(RespStatusEnum.CLIENT_BAD_PARAMETERS));
            return;
        }
        // 3、过滤接收者为null的请求参数
        List<MessageParam> resultMessageParamList = messageParamList.stream()
                                                    .filter(messageParam -> !StrUtil.isBlank(messageParam.getReceiver()))
                                                    .collect(Collectors.toList());
        if (CollUtil.isEmpty(resultMessageParamList)) {
            context.setNeedBreak(true).setResponse(BasicResultVO.fail(RespStatusEnum.CLIENT_BAD_PARAMETERS));
            return;
        }
        // 4、过滤接收者大于100的请求
        if (resultMessageParamList.stream().anyMatch(messageParam -> messageParam.getReceiver().split(StrUtil.COMMA).length > OpushConstant.BATCH_RECEIVER_SIZE)) {
            context.setNeedBreak(true).setResponse(BasicResultVO.fail(RespStatusEnum.TOO_MANY_RECEIVER));
            return;
        }
        sendTaskModel.setMessageParamList(resultMessageParamList);
    }
}
