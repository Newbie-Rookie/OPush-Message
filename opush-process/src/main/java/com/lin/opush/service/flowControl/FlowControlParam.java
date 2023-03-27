package com.lin.opush.service.flowControl;

import com.google.common.util.concurrent.RateLimiter;
import com.lin.opush.enums.FlowControlStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流量控制参数【限流器、限流器初始限流大小、限流策略】
 * 【由具体消息处理器初始化时创建并初始化】
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowControlParam {
    /**
     * 流量控制器
     */
    protected RateLimiter rateLimiter;

    /**
     * 流量控制器器初始限流大小
     */
    protected Double rateLimitInitValue;

    /**
     * 流量控制策略
     */
    protected FlowControlStrategy flowControlStrategy;
}
