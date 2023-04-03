package com.lin.opush.process.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.dto.model.WeChatMiniProgramContentModel;
import com.lin.opush.enums.ChannelType;
import com.lin.opush.process.BaseProcessor;
import com.lin.opush.process.Processor;
import com.lin.opush.utils.AccountUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 微信小程序发送订阅消息
 */
@Component
@Slf4j
public class WeChatMiniProgramProcessor extends BaseProcessor implements Processor {
    @Autowired
    private AccountUtils accountUtils;

    public WeChatMiniProgramProcessor() {
        channelTypeCode = ChannelType.MINI_PROGRAM.getCode();
    }

    /**
     * 发送微信小程序订阅信息
     * @param taskInfo 任务信息
     * @return 发送是否成功
     */
    @Override
    public boolean realSend(TaskInfo taskInfo) {
        // 微信小程序订阅信息发送内容模型
        WeChatMiniProgramContentModel contentModel = (WeChatMiniProgramContentModel) taskInfo.getContentModel();
        // 微信小程序配置
        WxMaService wxMaService = accountUtils.getAccountById(taskInfo.getSendAccount(), WxMaService.class);
        // 组装发送模板信息参数
        List<WxMaSubscribeMessage> wxMaSubscribeMessages = assembleReq(taskInfo.getReceiver(), contentModel);
        // 逐个发送订阅信息
        for (WxMaSubscribeMessage message : wxMaSubscribeMessages) {
            try {
                wxMaService.getSubscribeService().sendSubscribeMsg(message);
            } catch (Exception e) {
                log.info("WeChatMiniProgramProcessor#realSend fail! param:{},e:{}",
                        JSON.toJSONString(taskInfo), Throwables.getStackTraceAsString(e));
            }
        }
        return true;
    }

    /**
     * 组装发送模板信息参数
     * @param receiver 接收者
     * @param contentModel 内容模型
     * @return
     */
    private List<WxMaSubscribeMessage> assembleReq(Set<String> receiver, WeChatMiniProgramContentModel contentModel) {
        List<WxMaSubscribeMessage> messageList = new ArrayList<>(receiver.size());
        for (String openId : receiver) {
            WxMaSubscribeMessage subscribeMessage = WxMaSubscribeMessage.builder()
                                                    .toUser(openId).templateId(contentModel.getTemplateId())
                                                    .data(getWxMaTemplateData(contentModel.getMiniProgramParam()))
                                                    .page(contentModel.getPage()).build();
            messageList.add(subscribeMessage);
        }
        return messageList;
    }

    /**
     * 构建订阅消息参数
     * @param data 消息参数map
     * @return
     */
    private List<WxMaSubscribeMessage.MsgData> getWxMaTemplateData(Map<String, String> data) {
        List<WxMaSubscribeMessage.MsgData> templateDataList = new ArrayList<>(data.size());
        data.forEach((k, v) -> templateDataList.add(new WxMaSubscribeMessage.MsgData(k, v)));
        return templateDataList;
    }

    @Override
    public void recall(MessageTemplate messageTemplate) {

    }
}

