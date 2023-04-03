package com.lin.opush.constants;

/**
 * Flink常量信息
 */
public class FlinkConstant {
    // 数据源名
    public static final String SOURCE_NAME = "opush_kafka_source";
    // 转换算子名
    public static final String FUNCTION_NAME = "opush_transform_map";
    // 输出算子名
    public static final String SINK_NAME = "opush_sink_to_redis";
    // 作业名
    public static final String JOB_NAME = "OpushStreamProcessStart";

    /**
     * Kafka配置
     */
    public static final String GROUP_ID = "opushLogGroupId";
    public static final String TOPIC_NAME = "opushTraceLog";
    public static final String BROKER = "";

    /**
     * Redis配置
     */
    public static final String REDIS_IP = "";
    public static final String REDIS_PORT = "";
    public static final String REDIS_PASSWORD = "";
    public static final Integer REDIS_DATABASE = 1;
}
