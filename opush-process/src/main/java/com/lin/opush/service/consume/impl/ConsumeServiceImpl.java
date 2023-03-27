package com.lin.opush.service.consume.impl;

import cn.hutool.core.collection.CollUtil;

import com.lin.opush.domain.AnchorInfo;
import com.lin.opush.domain.LogParam;
import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.domain.sms.LoginUser;
import com.lin.opush.domain.sms.SmsReceipt;
import com.lin.opush.enums.AnchorState;
import com.lin.opush.domain.Task;
import com.lin.opush.process.ProcessorHolder;
import com.lin.opush.config.ThreadPoolHolder;
import com.lin.opush.script.impl.TencentSmsScript;
import com.lin.opush.script.impl.UniSmsScript;
import com.lin.opush.service.consume.ConsumeService;
import com.lin.opush.utils.GroupIdMappingUtils;
import com.lin.opush.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 消费消息服务实现（发送 / 撤回）
 */
@Service
public class ConsumeServiceImpl implements ConsumeService {
    /**
     * log标记业务类型
     */
    private static final String LOG_BIZ_TYPE = "Receiver#consumer";
    private static final String LOG_BIZ_RECALL_TYPE = "Receiver#recall";

    /**
     * 应用上下文
     */
    @Autowired
    private ApplicationContext context;

    /**
     * 存储不同渠道不同消息类型对应的线程池
     */
    @Autowired
    private ThreadPoolHolder threadPoolHolder;

    /**
     * 存储不同发送渠道对应的处理器
     */
    @Autowired
    private ProcessorHolder processorHolder;

    /**
     * UniSMS脚本【处理UniSMS短信回执】
     */
    @Autowired
    private UniSmsScript uniSmsScript;

    /**
     * 腾讯云脚本【发送登录验证码短信】
     */
    @Autowired
    private TencentSmsScript tencentSmsScript;

    /**
     * 记录日志工具类
     */
    @Autowired
    private LogUtils logUtils;

    /**
     * 处理发送消息任务
     * @param taskInfoLists 任务信息列表
     */
    @Override
    public void consumeToSend(List<TaskInfo> taskInfoLists) {
        // 根据第一个任务信息对象获取对应的groupId
        String groupId = GroupIdMappingUtils.getGroupIdByTaskInfo(CollUtil.getFirst(taskInfoLists.iterator()));
        // 遍历任务信息列表
        for (TaskInfo taskInfo : taskInfoLists) {
            // 记录任务发送日志并埋点
            logUtils.print(LogParam.builder().bizType(LOG_BIZ_TYPE).object(taskInfo).build(),
                                            AnchorInfo.builder().ids(taskInfo.getReceiver())
                                                                .businessId(taskInfo.getBusinessId())
                                                                .state(AnchorState.RECEIVE.getCode()).build());
            // 创建任务（run方法）
            Task task = context.getBean(Task.class).setTaskInfo(taskInfo);
            // 根据groupId获取对应线程池执行任务
            threadPoolHolder.route(groupId).execute(task);
        }
    }

    /**
     * 处理撤回消息任务
     * @param messageTemplate 消息模板
     */
    @Override
    public void consumeToRecall(MessageTemplate messageTemplate) {
        // 记录任务撤回日志
        logUtils.print(LogParam.builder().bizType(LOG_BIZ_RECALL_TYPE).object(messageTemplate).build());
        // 根据发送渠道获取处理器处理撤回任务
        processorHolder.route(messageTemplate.getSendChannel()).recall(messageTemplate);
    }

    /**
     * 保存短信回执
     * @param receipt 短信回执
     */
    @Override
    public void consumeToSave(SmsReceipt receipt) {
        // 处理短信回执
        uniSmsScript.saveSmsReceipt(receipt);
    }

    /**
     * 发送登录验证码
     * @param loginUser 登录用户
     */
    @Override
    public void consumeToSendLoginSms(LoginUser loginUser) {
        // 发送登录验证码短信
        tencentSmsScript.sendLoginSms(loginUser);
    }
}
