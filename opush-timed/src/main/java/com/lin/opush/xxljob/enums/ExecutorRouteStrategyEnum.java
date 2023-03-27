package com.lin.opush.xxljob.enums;

/**
 * 执行器路由策略枚举
 */
public enum ExecutorRouteStrategyEnum {
    /**
     * 第一个
     */
    FIRST,
    /**
     * 最后一个
     */
    LAST,
    /**
     * 轮询
     */
    ROUND,
    /**
     * 随机
     */
    RANDOM,
    /**
     * 一致性Hash
     * 【每个任务按照Hash算法固定选择某台机器，且所有任务均匀散列在不同机器上】
     */
    CONSISTENT_HASH,
    /**
     * 最不经常使用
     */
    LEAST_FREQUENTLY_USED,
    /**
     * 最近最少使用
     */
    LEAST_RECENTLY_USED,
    /**
     * 故障转移
     * 【按顺序进行心跳检测，选择第一个心跳检测成功的机器】
     */
    FAILOVER,
    /**
     * 忙碌转移
     * 【按顺序进行空闲检测，选择第一个空闲检测成功的机器】
     */
    BUSYOVER,
    /**
     * 分片广播
     * 【广播触发对应集群中所有机器执行一次任务，同时系统自动传递分片参数，可根据分片参数开发分片任务】
     */
    SHARDING_BROADCAST;

    ExecutorRouteStrategyEnum() {}
}
