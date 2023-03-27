package com.lin.opush.service.discard;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.lin.opush.constants.CommonConstant;
import com.lin.opush.domain.AnchorInfo;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.enums.AnchorState;
import com.lin.opush.service.ConfigService;
import com.lin.opush.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消息丢弃服务
 *      暂时支持根据模板id进行消息丢弃
 */
@Service
public class MessageDiscardService {
    private static final String DISCARD_MESSAGE_KEY = "discardMsgTpIds";

    @Autowired
    private ConfigService config;

    @Autowired
    private LogUtils logUtils;

    /**
     * 丢弃消息
     * 暂时支持根据模板id进行消息丢弃
     * 暂时配置在本地，后续会接入远程配置服务（Apollo，Nacos）
     * @param taskInfo 任务信息
     * @return
     */
    public boolean isDiscard(TaskInfo taskInfo) {
        // 将JSON字符串解析为数组
        // 配置示例:	["1","2"]
        JSONArray array = JSON.parseArray(config.getProperty(DISCARD_MESSAGE_KEY, CommonConstant.EMPTY_VALUE_JSON_ARRAY));
        // 判断数组中是否存在当前任务信息中的消息模板id（有则丢弃）
        if (array.contains(String.valueOf(taskInfo.getMessageTemplateId()))) {
            logUtils.print(AnchorInfo.builder().businessId(taskInfo.getBusinessId()).ids(taskInfo.getReceiver()).state(AnchorState.DISCARD.getCode()).build());
            return true;
        }
        return false;
    }
}
