package com.lin.opush.config;

import cn.hutool.core.thread.ExecutorBuilder;
import com.lin.opush.constants.ThreadPoolConstant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * support模块线程池配置类
 */
public class SupportThreadPoolConfig {
    /**
     * 获取线程池【处理拉取回执等任务】
     * 核心线程可被回收，当线程池无被引用且无核心线程时，应当被回收
     * 动态线程池且被Spring管理：false
     */
    public static ExecutorService getThreadPool() {
        return ExecutorBuilder.create()
                // 线程池核心线程数（1个）
                .setCorePoolSize(ThreadPoolConstant.SINGLE_CORE_POOL_SIZE)
                // 线程池最大线程数（1个）
                .setMaxPoolSize(ThreadPoolConstant.SINGLE_MAX_POOL_SIZE)
                // 工作队列
                // LinkedBlockingQueue链式阻塞队列，长度1024
                .setWorkQueue(ThreadPoolConstant.BIG_BLOCKING_QUEUE)
                // 任务拒绝策略
                // 使用CallerRunsPolicy：在调用者线程中直接执行被拒绝任务的run方法，除非线程池已shutdown，则抛弃任务
                .setHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                // 是否允许核心线程空闲退出（默认false）
                .setAllowCoreThreadTimeOut(true)
                // 空闲线程存活时间（10s）
                .setKeepAliveTime(ThreadPoolConstant.SMALL_KEEP_LIVE_TIME, TimeUnit.SECONDS)
                .build();
    }
}
