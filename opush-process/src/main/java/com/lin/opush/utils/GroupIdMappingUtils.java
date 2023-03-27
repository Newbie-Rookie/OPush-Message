package com.lin.opush.utils;

import com.lin.opush.domain.TaskInfo;
import com.lin.opush.enums.ChannelType;
import com.lin.opush.enums.MessageType;

import java.util.ArrayList;
import java.util.List;

/**
 * 生成和获取不同渠道不同消息类型的groupId工具类
 * （一个groupId唯一标识一个消费者组）
 */
public class GroupIdMappingUtils {
    /**
     * 生成不同渠道（短信、邮件...）的不同的消息类型（通知类、营销类、验证码）对应的groupId
     * groupId形式：渠道类型.消息类型
     * @return 所有消费者组的groupId列表
     */
    public static List<String> generateGroupIds() {
        List<String> groupIds = new ArrayList<>();
        // 渠道类型
        for (ChannelType channelType : ChannelType.values()) {
            // 消息类型
            for (MessageType messageType : MessageType.values()) {
                groupIds.add(channelType.getCodeEn() + "." + messageType.getCodeEn());
            }
        }
        return groupIds;
    }

    /**
     * 根据任务信息获取对应的groupId
     * @param taskInfo 任务信息
     * @return groupId
     */
    public static String getGroupIdByTaskInfo(TaskInfo taskInfo) {
        // 渠道类型编码
        String channelCodeEn = ChannelType.getEnumByCode(taskInfo.getSendChannel()).getCodeEn();
        // 消息类型编码
        String msgCodeEn = MessageType.getEnumByCode(taskInfo.getMsgType()).getCodeEn();
        // 返回groupId
        return channelCodeEn + "." + msgCodeEn;
    }
}
