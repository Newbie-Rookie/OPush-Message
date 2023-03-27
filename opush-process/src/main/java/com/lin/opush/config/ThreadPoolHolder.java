package com.lin.opush.config;

import com.dtp.core.thread.DtpExecutor;
import com.lin.opush.utils.GroupIdMappingUtils;
import com.lin.opush.utils.ThreadPoolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 初始化不同渠道不同消息类型对应的线程池
 * 使用Map存储不同渠道不同消息类型与处理不同渠道不同消息类型的线程池之间的映射
 * 提供路由方法获取不同渠道不同消息类型对应的线程池
 */
@Component
public class ThreadPoolHolder {
    /**
     * 生成不同渠道（短信、邮件...）的不同消息类型（通知类、营销类、验证码）对应的groupId
     */
    private static List<String> groupIds = GroupIdMappingUtils.generateGroupIds();

    /**
     * 线程池工具类
     */
    @Autowired
    private ThreadPoolUtils threadPoolUtils;

    /**
     * 存储不同渠道不同消息类型与处理不同渠道不同消息类型的线程池之间的映射
     */
    private Map<String, ExecutorService> threadPoolHolder = new HashMap<>(32);

    /**
     * 初始化不同渠道不同消息类型对应的线程池
     * 注册到动态线程池中并将线程池关闭交由spring容器管理，实现优雅关闭
     */
    @PostConstruct
    public void init() {
        for (String groupId : groupIds) {
            // 初始化线程池
            DtpExecutor executor = ProcessThreadPoolConfig.getExecutor(groupId);
            // 将线程池加入动态线程池内，并将线程池关闭交由spring容器管理，实现优雅关闭
            threadPoolUtils.register(executor);
            // 将groupId与线程池的映射关系放入map中
            threadPoolHolder.put(groupId, executor);
        }
    }

    /**
     * 路由方法：根据groupId获取对应的线程池
     * @param groupId groupId
     * @return 线程池
     */
    public ExecutorService route(String groupId) {
        return threadPoolHolder.get(groupId);
    }
}
