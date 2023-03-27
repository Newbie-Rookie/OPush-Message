package com.lin.opush.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReUtil;
import com.alibaba.fastjson.JSON;
import com.lin.opush.constants.RegexConstant;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.enums.IdType;
import com.lin.opush.enums.RespStatusEnum;
import com.lin.opush.chain.ExecutionAction;
import com.lin.opush.chain.ExecutionChainContext;
import com.lin.opush.domain.SendTaskModel;
import com.lin.opush.vo.BasicResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 后置参数检查
 */
@Slf4j
@Service
public class AfterParamCheckAction implements ExecutionAction<SendTaskModel> {
    // 存储不同渠道对应的正则表达式常量
    public static final HashMap<Integer, String> CHANNEL_REGEX = new HashMap<>();

    static {
        CHANNEL_REGEX.put(IdType.PHONE.getCode(), RegexConstant.PHONE_REGEX);
        CHANNEL_REGEX.put(IdType.EMAIL.getCode(), RegexConstant.EMAIL_REGEX);
    }

    @Override
    public void execute(ExecutionChainContext<SendTaskModel> context) {
        SendTaskModel sendTaskModel = context.getExecutionChainDataModel();
        List<TaskInfo> taskInfo = sendTaskModel.getTaskInfo();
        // 过滤掉不合法的手机号、邮件的发送任务
        filterIllegalReceiver(taskInfo);
        // 判断发送任务列表过滤后是否为空
        if (CollUtil.isEmpty(taskInfo)) {
            context.setNeedBreak(true).setResponse(BasicResultVO.fail(RespStatusEnum.CLIENT_BAD_PARAMETERS));
        }
    }

    /**
     * 如果指定类型是手机号，检测输入手机号是否合法
     * 如果指定类型是邮件，检测输入邮件是否合法
     * @param taskInfo 发送任务列表
     */
    private void filterIllegalReceiver(List<TaskInfo> taskInfo) {
        // 根据接收者id类型获取对应的正则表达式
        Integer idType = CollUtil.getFirst(taskInfo.iterator()).getIdType();
        filter(taskInfo, CHANNEL_REGEX.get(idType));
    }

    /**
     * 利用正则过滤掉不合法的接收者
     * @param taskInfo 发送任务列表
     * @param regexExp 正则表达式
     */
    private void filter(List<TaskInfo> taskInfo, String regexExp) {
        Iterator<TaskInfo> iterator = taskInfo.iterator();
        while (iterator.hasNext()) {
            TaskInfo task = iterator.next();
            // 过滤每一个发送任务中的接收者set集合
            // 获取不合法接收者集合（邮件、手机号）
            Set<String> illegalReceiverValues = task.getReceiver().stream()
                                        .filter(receiverValue -> !ReUtil.isMatch(regexExp, receiverValue))
                                        .collect(Collectors.toSet());
            // 不为空则移除这些不合法的接收者
            if (CollUtil.isNotEmpty(illegalReceiverValues)) {
                task.getReceiver().removeAll(illegalReceiverValues);
                log.error("messageTemplateId:{} find illegal receiver!{}", task.getMessageTemplateId(), JSON.toJSONString(illegalReceiverValues));
            }
            // 如果该发送任务中所有接收者都不合法则移除该发送任务
            if (CollUtil.isEmpty(task.getReceiver())) {
                iterator.remove();
            }
        }
    }
}
