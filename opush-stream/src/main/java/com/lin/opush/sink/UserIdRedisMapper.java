//package com.lin.opush.sink;
//
//import cn.hutool.core.date.DateUtil;
//import com.alibaba.fastjson.JSON;
//import com.lin.opush.domain.SimpleAnchorInfo;
//import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
//import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
//import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;
//
//import java.util.Date;
//
///**
// * 用户维度：使用List类型存储每个用户埋点信息
// *         【key为creator:userId，value为每个用户埋点信息的JSON字符串】
// * 【由于flink-connector-redis的LPUSH命令不支持设置过期时间，该类弃用】
// */
//public class UserIdRedisMapper implements RedisMapper<SimpleAnchorInfo> {
//    /**
//     * 获取Redis指令描述
//     * @return Redis指令描述
//     */
//    @Override
//    public RedisCommandDescription getCommandDescription() {
//        return new RedisCommandDescription(RedisCommand.LPUSH,
//            Integer.parseInt(
//                String.valueOf(
//                        (DateUtil.endOfDay(new Date()).getTime() - DateUtil.current()) / 1000)
//                )
//            );
//    }
//
//    /**
//     * 获取key
//     * @param simpleAnchorInfo 埋点信息【单用户】
//     * @return key
//     */
//    @Override
//    public String getKeyFromData(SimpleAnchorInfo simpleAnchorInfo) {
//        return simpleAnchorInfo.getCreator() + ":" + simpleAnchorInfo.getId();
//    }
//
//    /**
//     * 获取value
//     * @param simpleAnchorInfo 埋点信息【单用户】
//     * @return value
//     */
//    @Override
//    public String getValueFromData(SimpleAnchorInfo simpleAnchorInfo) {
//        return JSON.toJSONString(simpleAnchorInfo);
//    }
//}
