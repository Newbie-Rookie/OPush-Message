package com.lin.opush.utils;

import com.lin.opush.callback.RedisPipelineCallBack;
import com.lin.opush.constants.FlinkConstant;
import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.ByteArrayCodec;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 由于需在无Spring环境下使用Redis，对Lettuce进行封装
 * 【使用管道操作节省网络IO开销，使用字节数组传输】
 * 【Redis命令的异步API + 手动批量推送实现pipeline】
 */
public class LettuceRedisUtils {
    /**
     * 初始化RedisClient
     */
    private static RedisClient redisClient;

    static {
        RedisURI redisUri = RedisURI.Builder
                            .redis(FlinkConstant.REDIS_IP)
                            .withPort(Integer.valueOf(FlinkConstant.REDIS_PORT))
                            .withPassword(FlinkConstant.REDIS_PASSWORD.toCharArray())
                            .withDatabase(FlinkConstant.REDIS_DATABASE).build();
        redisClient = RedisClient.create(redisUri);
    }

    /**
     * 封装pipeline【管道】操作
     * 【Redis命令的异步API + 手动推送批量命令实现pipeline】
     */
    public static void pipeline(RedisPipelineCallBack pipelineCallBack) {
        // 与Redis建立连接【使用byte[]作为键和值】
        StatefulRedisConnection<byte[], byte[]> connect = redisClient.connect(new ByteArrayCodec());
        // 获取异步API
        RedisAsyncCommands<byte[], byte[]> commands = connect.async();
        // 缓存批量命令
        List<RedisFuture<?>> redisFutures = pipelineCallBack.invoke(commands);
        // 手动推送批量命令
        commands.flushCommands();
        // 阻塞同步
        LettuceFutures.awaitAll(10, TimeUnit.SECONDS, redisFutures.toArray(new RedisFuture[redisFutures.size()]));
        // 关闭连接
        connect.close();
    }
}
