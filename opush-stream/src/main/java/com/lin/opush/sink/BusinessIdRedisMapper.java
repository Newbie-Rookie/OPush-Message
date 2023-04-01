//package com.lin.opush.sink;
//
//import cn.hutool.core.date.DateUtil;
//import com.lin.opush.domain.AnchorInfo;
//import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
//import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
//import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;
//
//import java.util.Date;
//import java.util.Optional;
//
///**
// * 模板维度：使用Hash类型存储多用户埋点信息将多用户埋点信息的JSON字符串转换为多用户埋点信息对象
// *         【key为creator:businessId，field为具体点位，value为步长(发送人数)】
// * 【由于flink-connector-redis的LPUSH命令不支持设置过期时间，该类弃用】
// */
//public class BusinessIdRedisMapper implements RedisMapper<AnchorInfo> {
//    /**
//     * 获取Redis指令描述
//     * @return Redis指令描述
//     */
//    @Override
//    public RedisCommandDescription getCommandDescription() {
//        return new RedisCommandDescription(RedisCommand.HINCRBY,
//            Integer.parseInt(
//                String.valueOf(
//                        (DateUtil.offsetDay(new Date(), 30).getTime() - DateUtil.current()) / 1000)
//                )
//            );
//    }
//
//    /**
//     * 获取key
//     * @param anchorInfo 埋点信息【多用户】
//     * @return key
//     */
//    @Override
//    public Optional<String> getAdditionalKey(AnchorInfo anchorInfo) {
//        String key = anchorInfo.getCreator() + ":" + anchorInfo.getBusinessId();
//        return Optional.of(key);
//    }
//
//    /**
//     * 获取field
//     * @param anchorInfo 埋点信息【多用户】
//     * @return field
//     */
//    @Override
//    public String getKeyFromData(AnchorInfo anchorInfo) {
//        return String.valueOf(anchorInfo.getState());
//    }
//
//    /**
//     * 获取value
//     * @param anchorInfo 埋点信息【多用户】
//     * @return value
//     */
//    @Override
//    public String getValueFromData(AnchorInfo anchorInfo) {
//        return String.valueOf(anchorInfo.getIds().size());
//    }
//}
