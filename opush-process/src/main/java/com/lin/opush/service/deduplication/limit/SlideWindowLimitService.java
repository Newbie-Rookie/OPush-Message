package com.lin.opush.service.deduplication.limit;

import cn.hutool.core.util.IdUtil;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.service.deduplication.DeduplicationParam;
import com.lin.opush.service.deduplication.deduplicationService.AbstractDeduplicationService;
import com.lin.opush.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 滑动窗口去重器（内容去重采用基于redis中zset的滑动窗口去重，可以做到严格控制单位时间内的频次）
 */
@Service(value = "SlideWindowLimitService")
public class SlideWindowLimitService extends AbstractLimitService {
    /**
     * 限流算法标记前缀
     */
    private static final String LIMIT_TAG = "SW_";

    /**
     * Redis工具类
     */
    @Autowired
    private RedisUtils redisUtils;

    /**
     * RedisScript的默认实现
     * 用于发送Lua脚本到Redis服务器上执行
     */
    private DefaultRedisScript<Long> defaultRedisScript;

    /**
     * 初始化DefaultRedisScript
     */
    @PostConstruct
    public void init() {
        defaultRedisScript = new DefaultRedisScript();
        // 设置返回类型（必须设置）
        defaultRedisScript.setResultType(Long.class);
        // 设置lua脚本资源（limit.lua）
        defaultRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("limit.lua")));
    }

    /**
     * 滑动窗口实现限流
     * 使用String类型的key和value存入Redis
     * @param service 去重服务（内容去重服务）
     * @param taskInfo 任务信息
     * @param deduplicationParam 去重参数（内容去重参数）
     * @return 返回不符合条件的手机号码
     */
    @Override
    public Set<String> limit(AbstractDeduplicationService service, TaskInfo taskInfo, DeduplicationParam deduplicationParam) {
        // 存储需要去重的接收者
        Set<String> deduplicationReceiver = new HashSet<>(taskInfo.getReceiver().size());
        // 遍历所有接收者
        for (String receiver : taskInfo.getReceiver()) {
            // Redis执行lua脚本判断当前接收者是否在滑动窗口内（是否在短时间内已接受过消息，在则将该接收者放入deduplicationReceiver）
            // redisUtils.execLimitLua(RedisScript<Long> redisScript, List<String> keys, String... args)
            /*
                defaultRedisScript：用于发送Lua脚本到Redis服务器上执行
                KEYS[...]：
                    KEYS[1]：接收者在Redis中对应的key(SW_ + md5(templateId + receiver + content))
                ARGV[...]：
                    ARGV[1]：时间段(5分钟，300s) * 1000 = 300000ms
                    ARGV[2]：当前时间(单位ms)
                    ARGV[3]：阈值(短时间[300s]内的发送次数，当前为1)
                    ARGV[4]：获取全局单例的Snowflake对象利用雪花算法生成分布式全局唯一ID(64位，生成的所有id随时间递增)
                                第1位：符号位，1表示负数，0表示正数(由于我们生成的雪花算法都是正整数，所以这里是0)
                                第2 ~ 42位：时间戳(共41位，0 ~ 2^41-1，大约69年) = 当前时间戳 - 起始时间戳(固定)
                                第43 ~ 52位：工作机器id(共10位，可部署在1024台机器上，10位又可分为前五位为数据中心id，后五位是机器id)
                                后12位：序列号(0 ~ 2^12-1，共4096个数)
             */
            if (redisUtils.execLimitLua(
                            defaultRedisScript,
                            Collections.singletonList(LIMIT_TAG + getSingleDeduplicationKey(service, taskInfo, receiver)),
                            String.valueOf(deduplicationParam.getDeduplicationTimeQuantum() * 1000),
                            String.valueOf(System.currentTimeMillis()),
                            String.valueOf(deduplicationParam.getDediplicationTimes()),
                            String.valueOf(IdUtil.getSnowflake().nextId()))) {
                deduplicationReceiver.add(receiver);
            }
        }
        return deduplicationReceiver;
    }
}
