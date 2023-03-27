package com.lin.opush.xxljob.enums;

/**
 * 调度过期策略枚举
 */
public enum ScheduleOverdueStrategyEnum {
    /**
     * 忽略
     */
    DO_NOTHING,

    /**
     * 立即执行一次
     */
    FIRE_ONCE_NOW;

    ScheduleOverdueStrategyEnum() {}
}
