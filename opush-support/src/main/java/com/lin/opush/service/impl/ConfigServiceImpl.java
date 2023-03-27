package com.lin.opush.service.impl;

import cn.hutool.setting.dialect.Props;
import com.lin.opush.service.ConfigService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * 配置服务实现
 */
@Service
public class ConfigServiceImpl implements ConfigService {
    /**
     * 暂时使用本地配置（后续可接入远程配置服务Apollo、Nacos）
     */
    private static final String PROPERTIES_PATH = "opush.properties";

    /**
     * 读取指定路径的配置文件（UTF-8）并转化为对应的配置对象
     */
    private Props props = new Props(PROPERTIES_PATH, StandardCharsets.UTF_8);

    @Override
    public String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}
