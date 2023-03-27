package com.lin.opush.service;

/**
 * 处理定时任务接口
 */
public interface ProcessTimedJob {
    /**
     * 处理定时任务
     * @param creator 消息模板创建者
     * @param messageTemplateId 消息模板id
     */
    void process(String creator, Long messageTemplateId);
}
