package com.lin.opush.mq.kafka;

import com.lin.opush.constants.MessageQueueType;
import com.lin.opush.mq.SendMqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * 发送实现类：Kafka
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "opush.mq.type", havingValue = MessageQueueType.KAFKA)
public class KafkaSendMqServiceImpl implements SendMqService {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 发送消息
     * @param topic 发送给Kafka的主题
     * @param json 任务信息列表 / 消息回执的JSON字符串【为null时发送登录短信】
     */
    @Override
    public void send(String topic, String json) {
        kafkaTemplate.send(topic, json);
    }
}
