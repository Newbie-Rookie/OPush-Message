package com.lin.opush.sink;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.lin.opush.constants.CommonConstant;
import com.lin.opush.domain.AnchorInfo;
import com.lin.opush.domain.SimpleAnchorInfo;
import com.lin.opush.utils.LettuceRedisUtils;
import io.lettuce.core.RedisFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 实时数据多维度【用户维度、模板维度】写入Redis
 */
@Slf4j
public class OpushRedisSink implements SinkFunction<AnchorInfo> {
    @Override
    public void invoke(AnchorInfo anchorInfo, Context context) {
        try {
            LettuceRedisUtils.pipeline(redisAsyncCommands -> {
                List<RedisFuture<?>> redisFutures = new ArrayList<>();
                /**
                 * 用户维度：使用List类型存储每个用户埋点信息
                 *         【key为creator:userId，value为每个用户埋点信息的JSON字符串】
                 */
                SimpleAnchorInfo simpleAnchorInfo = SimpleAnchorInfo.builder()
                                                    .state(anchorInfo.getState())
                                                    .businessId(anchorInfo.getBusinessId())
                                                    .logTimestamp(anchorInfo.getLogTimestamp())
                                                    .build();
                for (String id : anchorInfo.getIds()) {
                    redisFutures.add(redisAsyncCommands.lpush(
                                (anchorInfo.getCreator() + CommonConstant.COLON + id).getBytes(),
                                JSON.toJSONString(simpleAnchorInfo).getBytes()));
                    redisFutures.add(redisAsyncCommands.expire(
                                (anchorInfo.getCreator() + CommonConstant.COLON + id).getBytes(),
                            (DateUtil.endOfDay(new Date()).getTime() - DateUtil.current()) / 1000));
                }
                /**
                 * 模板维度：使用Hash类型存储多用户埋点信息将多用户埋点信息的JSON字符串转换为多用户埋点信息对象
                 *         【key为creator:businessId，field为具体点位，value为步长(发送人数)】
                 */
                redisFutures.add(redisAsyncCommands.hincrby(
                    (anchorInfo.getCreator() + CommonConstant.COLON + anchorInfo.getBusinessId()).getBytes(),
                    String.valueOf(anchorInfo.getState()).getBytes(), anchorInfo.getIds().size()));
                redisFutures.add(redisAsyncCommands.expire(
                    (anchorInfo.getCreator() + CommonConstant.COLON + anchorInfo.getBusinessId()).getBytes(),
                    ((DateUtil.offsetDay(new Date(), 30).getTime() - DateUtil.currentSeconds()) / 1000)));
                return redisFutures;
            });
        } catch (Exception e) {
            log.error("OpushRedisSink#invoke error: {}", Throwables.getStackTraceAsString(e));
        }
    }
}
