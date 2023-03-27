package com.lin.opush.utils;

import com.dtp.core.DtpRegistry;
import com.dtp.core.thread.DtpExecutor;
import com.lin.opush.config.ThreadPoolShutdownGracefully;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 线程池工具类
 */
@Component
public class ThreadPoolUtils {
    /**
     * 线程池关闭
     */
    @Autowired
    private ThreadPoolShutdownGracefully shutdownDefinition;

    /**
     * 动态线程池中所有该项目线程池的资源名
     */
    private static final String SOURCE_NAME = "opush";

    /**
     * 将线程池加入到动态线程池内并将线程池关闭交由spring容器管理，实现优雅关闭
     * @param dtpExecutor 线程池
     */
    public void register(DtpExecutor dtpExecutor) {
        // DTP_REGISTRY.put(executor.getThreadPoolName(), executor)
        // 将线程池加入到动态线程池内
        DtpRegistry.register(dtpExecutor, SOURCE_NAME);
        // 将线程池关闭交由spring容器管理，进行优雅关闭
        shutdownDefinition.registryExecutor(dtpExecutor);
    }
}
