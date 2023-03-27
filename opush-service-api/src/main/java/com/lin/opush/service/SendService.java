package com.lin.opush.service;

import com.lin.opush.domain.BatchSendRequest;
import com.lin.opush.domain.SendRequest;
import com.lin.opush.domain.SendResponse;

/**
 * 单条/批量消息发送接口
 */
public interface SendService {
    /**
     * 单模板单文案发送接口
     * 【单文案下发不同的人（多个receiver用逗号隔开）】
     * 【对应一个TaskInfo，即List<TaskInfo>仅有一个元素】
     * 【对应一个Task】
     * @param sendRequest
     * @return
     */
    SendResponse send(SendRequest sendRequest);

    /**
     * 单模板多文案发送接口（减少多次远程调用）
     * 【多文案下发不同的人（多个receiver用逗号隔开），List<MessageParam>保存不同文案】
     * 【对应多个TaskInfo，即List<TaskInfo>】
     * 【对应多个Task】
     * @param batchSendRequest
     * @return
     */
    SendResponse batchSend(BatchSendRequest batchSendRequest);
}
