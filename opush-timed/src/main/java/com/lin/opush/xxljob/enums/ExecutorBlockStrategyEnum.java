package com.lin.opush.xxljob.enums;

/**
 * 执行器阻塞处理策略枚举
 */
public enum ExecutorBlockStrategyEnum {
    /**
     * 单机串行
     * 【调度请求进入单机执行器后，调度请求进入FIFO队列并以串行方式运行】
     */
    SERIAL_EXECUTION,

    /**
     * 丢弃后续调度
     * 【调度请求进入单机执行器后，发现执行器存在任务运行，本次请求将会被丢弃并标记为失败】
     */
    DISCARD_LATER,

    /**
     * 覆盖之前调度
     * 【调度请求进入单机执行器后，发现执行器存在任务运行，将会终止运行中的任务并清空队列，然后运行本次调度任务】
     */
    COVER_EARLY;

    ExecutorBlockStrategyEnum() {}
}
