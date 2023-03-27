package com.lin.opush.xxljob.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * xxl-job执行器配置类
 * 【application-dev.properties中opush.xxl.job.enabled需为true，才会加载该配置类】
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "opush.xxl.job.enabled", havingValue = "true")
public class XxlJobConfig {
    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;
    @Value("${xxl.job.executor.appname}")
    private String appName;
    @Value("${xxl.job.executor.ip}")
    private String ip;
    @Value("${xxl.job.executor.port}")
    private int port;
    @Value("${xxl.job.accessToken}")
    private String accessToken;
    @Value("${xxl.job.executor.logpath}")
    private String logPath;
    @Value("${xxl.job.executor.logretentiondays}")
    private int logRetentionDays;

    /**
     * 创建执行器类XxlJobSpringExecutor
     *      继承XxlJobExecutor
     *      实现ApplicationContextAware【获取应用上下文ApplicationContext】
     *      实现DisposableBean【重写destroy方法，释放资源】
     *      实现SmartInitializingSingleton【单例bean都初始化完成后，找出带有注解@XxlJob的job进行注册等一系列操作】
     * @return 执行器类交由Spring容器管理
     */
    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        // 创建XxlJobSpringExecutor执行器
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appName);
        xxlJobSpringExecutor.setIp(ip);
        xxlJobSpringExecutor.setPort(port);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);
        return xxlJobSpringExecutor;
    }
}
