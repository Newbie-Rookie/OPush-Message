package com.lin.opush;

import com.alibaba.fastjson.JSON;
import com.lin.opush.constants.FlinkConstant;
import com.lin.opush.domain.AnchorInfo;
import com.lin.opush.sink.*;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * Flink启动类【流处理】
 * 【实时处理日志信息【打点信息】】
 */
public class OpushStreamProcessStart {
    public static void main(String[] args) throws Exception {
        // 获取执行环境【自适应获取本地环境 / 远程集群环境】
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 获取KafkaSource【offset自动提交，重启则从头开始消费】并获取数据源【无水位线】
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                                .setTopics(FlinkConstant.TOPIC_NAME)
                                .setGroupId(FlinkConstant.GROUP_ID)
                                .setBootstrapServers(FlinkConstant.BROKER)
                                .setStartingOffsets(OffsetsInitializer.earliest())
                                .setValueOnlyDeserializer(new SimpleStringSchema()).build();
        DataStreamSource<String> dataStreamSource = env.fromSource(kafkaSource,
                                                WatermarkStrategy.noWatermarks(),
                                                    FlinkConstant.SOURCE_NAME);

        // 数据转换处理：将多用户埋点信息的JSON字符串转换为多用户埋点信息对象【map】
        SingleOutputStreamOperator<AnchorInfo> operator = dataStreamSource.map(
            (MapFunction<String, AnchorInfo>) (anchorInfoJSON) ->
                JSON.parseObject(anchorInfoJSON, AnchorInfo.class)
        ).name(FlinkConstant.FUNCTION_NAME);

        // 实时数据多维度写入Redis
        operator.addSink(new OpushRedisSink()).name(FlinkConstant.SINK_NAME);

        // 触发程序执行
        env.execute(FlinkConstant.JOB_NAME);
    }
}
