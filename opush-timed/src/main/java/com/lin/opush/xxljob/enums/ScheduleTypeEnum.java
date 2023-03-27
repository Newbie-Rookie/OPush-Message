package com.lin.opush.xxljob.enums;

/**
 * 调度类型枚举
 * 【无、Cron、固定速度】
 */
public enum ScheduleTypeEnum {
    /**
     * 不主动触发调度
     */
    NONE,
    /**
     * 根据Cron表达式进行调度
     */
    CRON,
    /**
     * 以固定速度调度【单位秒】
     */
    FIX_RATE;

    ScheduleTypeEnum() {}
}
