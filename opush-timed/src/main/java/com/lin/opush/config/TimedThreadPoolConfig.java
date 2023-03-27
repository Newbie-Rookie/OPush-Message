package com.lin.opush.config;

import cn.hutool.core.thread.ExecutorBuilder;
import com.dtp.common.em.QueueTypeEnum;
import com.dtp.common.em.RejectedTypeEnum;
import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.thread.ThreadPoolBuilder;
import com.lin.opush.constants.ThreadPoolConstant;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Timed模块线程池配置：
 * 【异步处理xxl-job-admin请求的线程池】
 *      由于读取文件和远程调用发送接口是相对耗时的，使用线程池做异步处理，及时返回xxl-job-admin，避免定时任务超时
 * 【消费阻塞队列（延迟处理参数）的线程池】
 *
 */
public class TimedThreadPoolConfig {
    /**
     * 异步处理xxl-job-admin请求的线程池名
     */
    public static final String ASYNC_PROCESS_XXL_REQUEST_THREAD_POOL_NAME = "async-process-xxl-request-thread-pool";

    /**
     * 异步处理xxl-job-admin请求的线程池
     * 不丢弃消息，核心线程数不会随着keepAliveTime而减少
     * 动态线程池关闭交由Spring管理，进行优雅关闭【防止xxl-job-admin请求丢失】
     * @return 动态线程池
     */
    public static DtpExecutor getAsyncProcessXxlRequestExecutor() {
        return ThreadPoolBuilder.newBuilder()
                        .threadPoolName(ASYNC_PROCESS_XXL_REQUEST_THREAD_POOL_NAME)
                        .corePoolSize(ThreadPoolConstant.COMMON_CORE_POOL_SIZE)
                        .maximumPoolSize(ThreadPoolConstant.COMMON_MAX_POOL_SIZE)
                        .keepAliveTime(ThreadPoolConstant.COMMON_KEEP_LIVE_TIME)
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

    /**
     * 消费阻塞队列【延迟处理参数中】的线程池
     * 核心线程可以被回收，当线程池无被引用且无核心线程数，应当被回收
     * 动态线程池关闭不交由Spring管理，由自己控制线程池关闭
     * @return 线程池
     */
    public static ExecutorService getConsumeBlockingQueueThreadPool() {
        return ExecutorBuilder.create()
                            .setCorePoolSize(ThreadPoolConstant.COMMON_CORE_POOL_SIZE)
                            .setMaxPoolSize(ThreadPoolConstant.COMMON_MAX_POOL_SIZE)
                            // 允许核心线程空闲退出
                            .setAllowCoreThreadTimeOut(true)
                            // 当线程空闲时间达到keepAliveTime，线程退出，直到线程数量等于corePoolSize
                            // 若allowCoreThreadTimeout设置为true，则所有线程均会退出直到线程数为0
                            .setKeepAliveTime(ThreadPoolConstant.SMALL_KEEP_LIVE_TIME, TimeUnit.SECONDS)
                            // 任务拒绝策略
                            // 使用CallerRunsPolicy：在调用者线程中直接执行被拒绝任务的run方法，除非线程池已shutdown，则抛弃任务
                            .setHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                            // 工作队列
                            // 使用LinkedBlockingQueue（长度1024）：容量可修改的链式阻塞队列
                            .setWorkQueue(ThreadPoolConstant.BIG_BLOCKING_QUEUE).build();
    }
}
