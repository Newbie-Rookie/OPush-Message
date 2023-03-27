package com.lin.opush.service.deduplication.limit;

import cn.hutool.core.collection.CollUtil;
import com.lin.opush.constants.CommonConstant;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.service.deduplication.DeduplicationParam;
import com.lin.opush.service.deduplication.deduplicationService.AbstractDeduplicationService;
import com.lin.opush.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 采用普通计数去重方法，限制的是每天发送的条数。
 */
@Service(value = "SimpleCountLimitService")
public class SimpleCountLimitService extends AbstractLimitService {
    /**
     * 限流算法标记前缀
     */
    private static final String LIMIT_TAG = "SC_";

    /**
     * Redis工具类
     */
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 普通计算实现限流
     * 使用String类型的key和value存入Redis
     * @param service 去重服务（频次去重服务）
     * @param taskInfo 任务信息
     * @param deduplicationParam 去重参数（频次去重参数）
     * @return 返回不符合条件的手机号码
     */
    @Override
    public Set<String> limit(AbstractDeduplicationService service, TaskInfo taskInfo, DeduplicationParam deduplicationParam) {
        // 存储需要去重的接收者
        Set<String> deduplicationReceiver = new HashSet(taskInfo.getReceiver().size());
        // 存储不符合去重条件的用户或今天第一次发送给该用户
        Map<String, String> updateFrequencyReceiver = new HashMap(taskInfo.getReceiver().size());
        // 存储所有去重key
        List<String> frequencyKeys = getAllDeduplicationKey(service, taskInfo).stream().map(key -> LIMIT_TAG + key).collect(Collectors.toList());
        // 根据LIMIT_TAG + 去重key获取Redis中的所有对应频次
        Map<String, String> frequencyKeyValues = redisUtils.mGet(frequencyKeys);
        // 遍历所有接收者
        for (String receiver : taskInfo.getReceiver()) {
            String frequencyKey = LIMIT_TAG + getSingleDeduplicationKey(service, taskInfo, receiver);
            String frequencyValue = frequencyKeyValues.get(frequencyKey);
            // 符合频次去重条件（一天内5次）的用户则加入去重接收者Set集合中
            if (Objects.nonNull(frequencyValue) && Integer.parseInt(frequencyValue) >= deduplicationParam.getDediplicationTimes()) {
                deduplicationReceiver.add(receiver);
            } else {
                // 存储不符合去重条件的用户或今天第一次发送给该用户
                updateFrequencyReceiver.put(receiver, frequencyKey);
            }
        }
        // 不符合去重条件的用户或今天第一次发送给该用户：需要更新Redis（无记录添加，有记录则累加次数）
        updateRedis(updateFrequencyReceiver, frequencyKeyValues, deduplicationParam.getDeduplicationTimeQuantum());
        return deduplicationReceiver;
    }

    /**
     * 更新Redis中的不满足去重条件的用户的频次
     * @param updateFrequencyReceiver  不符合去重条件的用户或今天第一次发送给该用户
     * @param frequencyKeyValues       Map<LIMIT_TAG + 去重key, 频次>
     * @param deduplicationTimeQuantum 过期时间
     */
    private void updateRedis(Map<String, String> updateFrequencyReceiver,
                            Map<String, String> frequencyKeyValues, Long deduplicationTimeQuantum) {
        Map<String, String> keyValues = new HashMap<>(updateFrequencyReceiver.size());
        for (Map.Entry<String, String> entry : updateFrequencyReceiver.entrySet()) {
            String frequencyKey = entry.getValue();
            // 判断该用户是否为今天第一次接收消息
            if (Objects.nonNull(frequencyKeyValues.get(frequencyKey))) {
                keyValues.put(frequencyKey, String.valueOf(Integer.parseInt(frequencyKeyValues.get(frequencyKey)) + 1));
            } else {
                keyValues.put(frequencyKey, String.valueOf(CommonConstant.TRUE));
            }
        }
        if (CollUtil.isNotEmpty(keyValues)) {
            // 设置过期时间（从现在开始1天）
            redisUtils.pipelineSetEx(keyValues, deduplicationTimeQuantum);
        }
    }
}
