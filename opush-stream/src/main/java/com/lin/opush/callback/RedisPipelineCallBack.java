package com.lin.opush.callback;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.List;

/**
 * redis pipeline接口定义
 */
public interface RedisPipelineCallBack {
    /**
     * 具体执行逻辑
     * @param redisAsyncCommands Redis命令的异步API
     * @return
     */
    List<RedisFuture<?>> invoke(RedisAsyncCommands redisAsyncCommands);
}
