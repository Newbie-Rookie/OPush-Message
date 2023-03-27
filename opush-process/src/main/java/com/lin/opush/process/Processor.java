package com.lin.opush.process;

import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.domain.TaskInfo;

/**
 * 处理器接口
 */
public interface Processor {
    /**
     * 发送消息
     * @param taskInfo 任务信息
     */
    void send(TaskInfo taskInfo);

    /**
     * 撤回消息
     * @param messageTemplate 消息模板
     * @return
     */
    void recall(MessageTemplate messageTemplate);
}
