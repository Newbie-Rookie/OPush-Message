package com.lin.opush.config;

import com.dtp.common.em.QueueTypeEnum;
import com.dtp.common.em.RejectedTypeEnum;
import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.thread.ThreadPoolBuilder;
import com.lin.opush.constants.ThreadPoolConstant;

import java.util.concurrent.TimeUnit;

/**
 * 发送消息【Process】的动态线程池的配置
 * 【多线程池、不同渠道不同消息类型具有不同线程池】
 */
public class ProcessThreadPoolConfig {
    /**
     * 存储每个渠道不同消息类型的线程池的Map中key的前缀
     */
    private static final String PREFIX = "opush.";

    /**
     * 创建处理某个渠道的某种类型消息的线程池
     *      拒绝策略尽可能不丢弃消息，核心线程空闲不会被回收
     *      动态线程池被Spring管理
     * @param groupId groupId
     * @return 动态线程池
     */
    public static DtpExecutor getExecutor(String groupId) {
        return ThreadPoolBuilder.newBuilder()
                // opush.sms.inform
                .threadPoolName(PREFIX + groupId)
                // 线程池核心线程数（2个，线程池最少线程数）
                .corePoolSize(ThreadPoolConstant.COMMON_CORE_POOL_SIZE)
                // 线程池最大线程数（5个，当核心线程不够用时创建）
                .maximumPoolSize(ThreadPoolConstant.COMMON_MAX_POOL_SIZE)
                // 空闲线程存活时间（120s，超过该时间且线程数大于MaximumPoolSize则回收空闲线程）
                .keepAliveTime(ThreadPoolConstant.COMMON_KEEP_LIVE_TIME)
                // 设置单位s
                .timeUnit(TimeUnit.SECONDS)
                // 任务拒绝策略
                // 使用CallerRunsPolicy：在调用者线程中直接执行被拒绝任务的run方法，除非线程池已shutdown，则抛弃任务
                .rejectedExecutionHandler(RejectedTypeEnum.CALLER_RUNS_POLICY.getName())
                // 工作队列
                // 使用VariableLinkedBlockingQueue（长度256）：容量可修改的链式阻塞队列
                .workQueue(QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE.getName(),
                                    ThreadPoolConstant.COMMON_QUEUE_SIZE, false)
                .buildDynamic();
    }
}
