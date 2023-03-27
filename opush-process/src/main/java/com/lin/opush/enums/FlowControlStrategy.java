package com.lin.opush.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 流量控制策略枚举
 */
@Getter
@ToString
@AllArgsConstructor
public enum FlowControlStrategy {
    /**
     * 根据真实请求数限流 (QPS）
     */
    REQUEST_NUM_RATE_LIMIT(10, "根据真实请求数限流"),
    /**
     * 根据发送用户数限流（人数限流）
     */
    SEND_USER_NUM_RATE_LIMIT(20, "根据发送用户数限流"),
    ;

    /**
     * 编码值
     */
    private final Integer code;

    /**
     * 描述
     */
    private final String description;
}
