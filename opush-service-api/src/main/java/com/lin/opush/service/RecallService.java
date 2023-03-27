package com.lin.opush.service;

import com.lin.opush.domain.SendRequest;
import com.lin.opush.domain.SendResponse;

/**
 * 消息撤回接口
 */
public interface RecallService {
    /**
     * 根据模板id撤回消息
     * @param sendRequest
     * @return
     */
    SendResponse recall(SendRequest sendRequest);
}
