package com.lin.opush.mq;

/**
 * 发送数据至MQ
 */
public interface SendMqService {
    /**
     * 发送消息
     * @param topic 发送给Kafka的主题
     * @param json 任务信息列表 / 消息回执的JSON字符串
     */
    void send(String topic, String json);
}
