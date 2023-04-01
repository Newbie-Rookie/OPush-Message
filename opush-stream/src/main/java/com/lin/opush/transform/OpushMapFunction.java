//package com.lin.opush.transform;
//
//import com.alibaba.fastjson.JSON;
//import com.lin.opush.domain.AnchorInfo;
//import org.apache.flink.api.common.functions.MapFunction;
//
///**
// * 数据转换处理【map】
// * 【由于flink-connector-redis的LPUSH命令不支持设置过期时间，该类弃用】
// */
//public class OpushMapFunction implements MapFunction<String, AnchorInfo> {
//    @Override
//    public AnchorInfo map(String anchorInfoJSON) throws Exception {
//        AnchorInfo anchorInfo = JSON.parseObject(anchorInfoJSON, AnchorInfo.class);
//        return anchorInfo;
//    }
//}
