package com.lin.opush.constants;

/**
 * 延迟处理常量
 */
public class DelayProcessConstant {
    /**
     * 阻塞队列大小
     */
    public static final Integer QUEUE_SIZE = 100;

    /**
     * batch【批量】触发执行的数量阈值【同接口限制最多的人数】
     */
    public static final Integer NUMBER_THRESHOLD = 100;

    /**
     * batch【批量】触发执行的时间阈值，单位毫秒【必填】
     */
    public static final Long TIME_THRESHOLD = 1000L;
}
