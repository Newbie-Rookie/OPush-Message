package com.lin.opush.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 消息业务类型常量
 */
@Getter
@ToString
@AllArgsConstructor
public enum BusinessCode {
    /**
     * 发送业务
     */
    SEND("send", "发送消息"),
    /**
     * 撤回业务
     */
    RECALL("recall", "撤回消息");
    /**
     * 关联不同的执行链 → ExecutionChain
     *      关联关系存储在Map中（com.lin.opush.chain.ExecutionController#executionChainConfig）
     */
    private final String code;
    /**
     * 业务类型说明
     */
    private final String description;
}
