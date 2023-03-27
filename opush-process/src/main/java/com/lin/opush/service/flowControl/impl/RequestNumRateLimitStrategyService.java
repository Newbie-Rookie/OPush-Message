package com.lin.opush.service.flowControl.impl;

import com.google.common.util.concurrent.RateLimiter;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.enums.FlowControlStrategy;
import com.lin.opush.service.flowControl.FlowControlParam;
import com.lin.opush.service.flowControl.FlowControlStrategyService;
import com.lin.opush.service.flowControl.annotations.LocalFlowControlStrategy;

/**
 * 根据真实请求数进行流量控制
 */
@LocalFlowControlStrategy(flowControlStrategy = FlowControlStrategy.REQUEST_NUM_RATE_LIMIT)
public class RequestNumRateLimitStrategyService implements FlowControlStrategyService {
    /**
     * 根据真实请求数进行流量控制
     * @param taskInfo         任务参数
     * @param flowControlParam 流量控制参数
     * @return
     */
    @Override
    public Double flowControl(TaskInfo taskInfo, FlowControlParam flowControlParam) {
        RateLimiter rateLimiter = flowControlParam.getRateLimiter();
        return rateLimiter.acquire(1);
    }
}
