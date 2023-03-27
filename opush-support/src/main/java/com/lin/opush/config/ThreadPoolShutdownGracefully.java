package com.lin.opush.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 由于线程池中的工作队列是基于内存的，当项目关闭时，若线程池直接关闭会导致任务丢失，造成消息丢失
 * 监听容器关闭事件【实现ApplicationListener<ContextClosedEvent>，重写onApplicationEvent方法】
 * 实现多线程池【发送消息任务线程池】优雅关闭
 */
@Component
@Slf4j
public class ThreadPoolShutdownGracefully implements ApplicationListener<ContextClosedEvent> {
    /**
     * synchronized加锁的多线程池数组【用于后续关闭】
     */
    private final List<ExecutorService> threadPools = Collections.synchronizedList(new ArrayList<>(12));

    /**
     * 线程中的任务在接收到应用关闭信号量后最多等待多久就强制终止
     * 给线程池工作队列中剩余任务预留完成的时间，到时间后线程池必须销毁
     */
    private final long AWAIT_TERMINATION = 20;

    /**
     * AWAIT_TERMINATION的单位【s】
     */
    private final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    /**
     * 注册线程池，将线程池关闭交由容器管理
     * @param executor
     */
    public void registryExecutor(ExecutorService executor) {
        threadPools.add(executor);
    }

    /**
     * 监听容器关闭，实现线程池优雅关闭
     * 本方法参考org.springframework.scheduling.concurrent.ExecutorConfigurationSupport#shutdown()
     * @param event 容器关闭事件
     */
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("容器关闭前处理线程池优雅关闭开始, 当前要处理的线程池数量为: {} >>>>>>>>>>>>>>>>", threadPools.size());
        if (CollectionUtils.isEmpty(threadPools)) {
            return;
        }
        // 遍历所有线程池
        for (ExecutorService pool : threadPools) {
            // 线程池尝试关闭（如果工作队列不为空则不会关闭）
            pool.shutdown();
            try {
                // 该线程池等待20s后关闭
                if (!pool.awaitTermination(AWAIT_TERMINATION, TIME_UNIT)) {
                    if (log.isWarnEnabled()) {
                        log.warn("Timed out while waiting for executor [{}] to terminate", pool);
                    }
                }
            } catch (InterruptedException ex) {
                if (log.isWarnEnabled()) {
                    log.warn("Timed out while waiting for executor [{}] to terminate", pool);
                }
                Thread.currentThread().interrupt();
            }
        }
    }
}
