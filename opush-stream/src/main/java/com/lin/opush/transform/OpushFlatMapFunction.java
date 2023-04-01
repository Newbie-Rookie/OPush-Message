//package com.lin.opush.transform;
//
//import com.alibaba.fastjson.JSON;
//import com.lin.opush.domain.AnchorInfo;
//import com.lin.opush.domain.SimpleAnchorInfo;
//import org.apache.flink.api.common.functions.FlatMapFunction;
//import org.apache.flink.util.Collector;
//
///**
// * 数据转换处理【flatMap】
// * 【由于flink-connector-redis的LPUSH命令不支持设置过期时间，该类弃用】
// */
//public class OpushFlatMapFunction implements FlatMapFunction<String, SimpleAnchorInfo> {
//    @Override
//    public void flatMap(String anchorInfoJSON, Collector<SimpleAnchorInfo> collector) throws Exception {
//        AnchorInfo anchorInfo = JSON.parseObject(anchorInfoJSON, AnchorInfo.class);
//        for (String id : anchorInfo.getIds()) {
//            SimpleAnchorInfo simpleAnchorInfo = SimpleAnchorInfo.builder()
//                                                .id(id)
//                                                .businessId(anchorInfo.getBusinessId())
//                                                .logTimestamp(anchorInfo.getLogTimestamp())
//                                                .state(anchorInfo.getState())
//                                                .creator(anchorInfo.getCreator())
//                                                .build();
//            collector.collect(simpleAnchorInfo);
//        }
//    }
//}
