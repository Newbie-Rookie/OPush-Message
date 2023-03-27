package com.lin.opush;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 * @author Lin
 */
@SpringBootApplication
public class OpushApplication {
    public static void main(String[] args) {
        /**
         * 若接入远程配置服务Apollo
         *      1、启动apollo
         *      2、将application-dev.properties的opush.apollo.enabled改为true
         *      3、开启下发配置
         */
        // System.setProperty("apollo.config-com.lin.opush.com.lin.opush.service", "http://opush.apollo.config:5001");
        SpringApplication.run(OpushApplication.class, args);
    }
}
