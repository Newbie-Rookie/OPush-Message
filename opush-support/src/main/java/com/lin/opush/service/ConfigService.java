package com.lin.opush.service;

/**
 * 配置服务接口
 *      暂时不接入远程配置服务（Apollo、Nacos后续可接入）
 *      暂时使用本地配置
 */
public interface ConfigService {
    /**
     * 读取配置
     * @param key 配置属性key
     * @param defaultValue 获取不到该配置项时使用的默认值
     * @return
     */
    String getProperty(String key, String defaultValue);
}
