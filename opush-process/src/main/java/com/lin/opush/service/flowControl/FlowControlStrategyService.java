package com.lin.opush.service.flowControl;

import com.lin.opush.domain.TaskInfo;

/**
 * 流量控制策略服务接口
 */
public interface FlowControlStrategyService {
    /**
     * 根据渠道进行流量控制
     * @param taskInfo 任务参数
     * @param flowControlParam 流量控制参数
     * @return 耗费的时间
     */
    Double flowControl(TaskInfo taskInfo, FlowControlParam flowControlParam);
}
