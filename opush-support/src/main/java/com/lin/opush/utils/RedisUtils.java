package com.lin.opush.utils;

import cn.hutool.core.collection.CollUtil;
import com.google.common.base.Throwables;
import com.lin.opush.constants.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 对Redis的一些操作二次封装
 */
@Component
@Slf4j
public class RedisUtils {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取key对应的value并封装为Map<key,value>
     * @param keys
     * @return
     */
    public Map<String, String> mGet(List<String> keys) {
        HashMap<String, String> result = new HashMap<>(keys.size());
        try {
            // 查询不到key则返回空数组
            List<String> value = stringRedisTemplate.opsForValue().multiGet(keys);
            if (CollUtil.isNotEmpty(value)) {
                for (int i = 0; i < keys.size(); i++) {
                    result.put(keys.get(i), value.get(i));
                }
            }
        } catch (Exception e) {
            log.error("RedisUtils#mGet fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        return result;
    }

    /**
     * hGetAll
     * @param key
     */
    public Map<Object, Object> hGetAll(String key) {
        try {
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
            return entries;
        } catch (Exception e) {
            log.error("RedisUtils#hGetAll fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        return null;
    }

    /**
     * lRange
     * @param key
     */
    public List<String> lRange(String key, long start, long end) {
        try {
            return stringRedisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("RedisUtils#lRange fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        return null;
    }

    /**
     * 管道批量设置key-value并设置过期时间
     * redis的读写速度十分快，所以系统瓶颈往往在网络通信中的延迟，redis可能会在很多时候处于空闲状态而等待命令的到达
     * 为了解决这个问题，可以使用Redis的管道，管道是一种通讯协议，类似一个队列批量执行一组命令
     * @param keyValues
     * @param seconds
     */
    public void pipelineSetEx(Map<String, String> keyValues, Long seconds) {
        try {
            // 管道批量设置key-value并设置过期时间
            stringRedisTemplate.executePipelined((RedisCallback<String>) connection -> {
                for (Map.Entry<String, String> keyValue : keyValues.entrySet()) {
                    connection.setEx(keyValue.getKey().getBytes(), seconds, keyValue.getValue().getBytes());
                }
                return null;
            });
        } catch (Exception e) {
            log.error("RedisUtils#pipelineSetEx fail! e:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 执行指定的lua脚本返回执行结果
     * --KEYS[1]: 限流key
     * --ARGV[1]: 限流窗口
     * --ARGV[2]: 当前时间戳（作为score）
     * --ARGV[3]: 阈值
     * --ARGV[4]: score对应的唯一value
     * @param redisScript 用于发送Lua脚本到Redis服务器上执行
     * @param keys 限流key
     * @param args 附带参数
     * @return 执行是否成功
     */
    public Boolean execLimitLua(RedisScript<Long> redisScript, List<String> keys, String... args) {
        try {
            // 执行lua脚本
            Long execute = stringRedisTemplate.execute(redisScript, keys, args);
            if (Objects.isNull(execute)) {
                return false;
            }
            return CommonConstant.TRUE.equals(execute.intValue());
        } catch (Exception e) {
            log.error("redis execLimitLua fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        return false;
    }
}
