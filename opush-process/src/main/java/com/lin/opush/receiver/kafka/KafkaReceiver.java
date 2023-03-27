package com.lin.opush.receiver.kafka;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;

import com.lin.opush.constants.MessageQueueType;
import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.domain.sms.LoginUser;
import com.lin.opush.domain.sms.SmsReceipt;
import com.lin.opush.service.consume.ConsumeService;
import com.lin.opush.utils.GroupIdMappingUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 消费MQ的消息
 *      多例 → 多个消费者组
 *      （不同渠道的不同消息类型使用一个消费者组，实现数据上的逻辑隔离）
 */
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@ConditionalOnProperty(name = "opush.mq.type", havingValue = MessageQueueType.KAFKA)
public class KafkaReceiver {
    @Autowired
    private ConsumeService consumeService;

    /**
     * 发送消息
     * @KafkaListener的groupId已在项目启动时赋值为不同渠道的不同消息类型对应的groupId
     * @param consumerRecord 从消息队列中拉取的数据
     * @param topicGroupId 消费者组id
     */
    @KafkaListener(topics = "#{'${opush.business.send.topic.name}'}")
    public void consumer(ConsumerRecord<?, String> consumerRecord, @Header(KafkaHeaders.GROUP_ID) String topicGroupId) {
        // 获取拉取的消息内容存入Optional容器
        Optional<String> kafkaMessage = Optional.ofNullable(consumerRecord.value());
        // Optional容器不为空
        if (kafkaMessage.isPresent()) {
            // 将任务信息列表的JSON字符串转换为任务信息列表对象
            List<TaskInfo> taskInfoLists = JSON.parseArray(kafkaMessage.get(), TaskInfo.class);
            // 根据第一个任务信息对象获取对应的groupId
            String messageGroupId = GroupIdMappingUtils.getGroupIdByTaskInfo(CollUtil.getFirst(taskInfoLists.iterator()));
            /**
             * 每个消费者组只消费它们关心的消息（不同渠道的不同消息类型）
             */
            if (topicGroupId.equals(messageGroupId)) {
                consumeService.consumeToSend(taskInfoLists);
            }
        }
    }

    /**
     * 撤回消息
     * @param consumerRecord 从消息队列中拉取的数据
     */
    @KafkaListener(topics = "#{'${opush.business.recall.topic.name}'}", groupId = "#{'${opush.business.recall.group.id}'}")
    public void recall(ConsumerRecord<?, String> consumerRecord) {
        // 获取拉取的消息内容存入Optional容器
        Optional<String> kafkaMessage = Optional.ofNullable(consumerRecord.value());
        // Optional容器不为空
        if (kafkaMessage.isPresent()) {
            // 将模板信息的JSON字符串转为模板信息对象
            MessageTemplate messageTemplate = JSON.parseObject(kafkaMessage.get(), MessageTemplate.class);
            consumeService.consumeToRecall(messageTemplate);
        }
    }

    /**
     * 保存短信回执
     * @param consumerRecord 从消息队列中拉取的数据
     */
    @KafkaListener(topics = "#{'${opush.business.receipt.topic.name}'}", groupId = "#{'${opush.business.receipt.group.id}'}")
    public void saveSmsReceipt(ConsumerRecord<?, String> consumerRecord) {
        // 获取拉取的消息内容存入Optional容器
        Optional<String> kafkaMessage = Optional.ofNullable(consumerRecord.value());
        // Optional容器不为空
        if (kafkaMessage.isPresent()) {
            // 将模板信息的JSON字符串转为短信回执对象
            SmsReceipt receipt = JSON.parseObject(kafkaMessage.get(), SmsReceipt.class);
            consumeService.consumeToSave(receipt);
        }
    }

    /**
     * 发送登录短信【指定腾讯云渠道发送】
     */
    @KafkaListener(topics = "#{'${opush.business.login.topic.name}'}", groupId = "#{'${opush.business.login.group.id}'}")
    public void sendLoginSms(ConsumerRecord<?, String> consumerRecord) {
        // 获取拉取的消息内容存入Optional容器
        Optional<String> kafkaMessage = Optional.ofNullable(consumerRecord.value());
        // Optional容器不为空
        if (kafkaMessage.isPresent()) {
            // 将模板信息的JSON字符串转为短信回执对象
            LoginUser loginUser = JSON.parseObject(kafkaMessage.get(), LoginUser.class);
            consumeService.consumeToSendLoginSms(loginUser);
        }
    }
}
