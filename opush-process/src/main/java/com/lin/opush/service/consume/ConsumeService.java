package com.lin.opush.service.consume;

import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.domain.sms.LoginUser;
import com.lin.opush.domain.sms.SmsReceipt;

import java.util.List;

/**
 * 消费消息服务（发送 / 撤回 / 保存）
 */
public interface ConsumeService {
    /**
     * 从消息拉到消息进行消费，发送消息
     * @param taskInfoLists 任务信息列表
     */
    void consumeToSend(List<TaskInfo> taskInfoLists);

    /**
     * 从MQ拉到消息进行消费，撤回消息
     * @param messageTemplate 消息模板
     */
    void consumeToRecall(MessageTemplate messageTemplate);

    /**
     * 从MQ拉到消息进行消费，保存短信回执
     * @param receipt 短信回执
     */
    void consumeToSave(SmsReceipt receipt);

    /**
     * 从MQ拉到消息进行消费，发送登录短信
     */
    void consumeToSendLoginSms(LoginUser loginUser);
}
