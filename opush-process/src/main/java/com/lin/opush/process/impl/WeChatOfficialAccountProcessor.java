package com.lin.opush.process.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.dto.model.wechat.WeChatOfficialAccountsContentModel;
import com.lin.opush.enums.ChannelType;
import com.lin.opush.process.BaseProcessor;
import com.lin.opush.process.Processor;
import com.lin.opush.utils.AccountUtils;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 微信服务号发送模板消息
 */
@Component
@Slf4j
public class WeChatOfficialAccountProcessor extends BaseProcessor implements Processor {
    /**
     * 渠道账号工具类
     */
    @Autowired
    private AccountUtils accountUtils;

    public WeChatOfficialAccountProcessor() {
        channelTypeCode = ChannelType.WACHAT_OFFICIAL_ACCOUNT.getCode();
    }

    /**
     * 发送微信服务号模板信息
     * @param taskInfo 任务信息
     * @return 发送是否成功
     */
    @Override
    public boolean realSend(TaskInfo taskInfo) {
        try {
            // 微信服务号模板信息发送内容模型
            WeChatOfficialAccountsContentModel contentModel = (WeChatOfficialAccountsContentModel) taskInfo.getContentModel();
            // 微信服务号配置
            WxMpService wxMpService = accountUtils.getAccountById(taskInfo.getSendAccount(), WxMpService.class);
            // 组装发送模板信息参数
            List<WxMpTemplateMessage> wxMpTemplateMessages = assembleReq(taskInfo.getReceiver(), contentModel);
            for (WxMpTemplateMessage wxMpTemplateMessage : wxMpTemplateMessages) {
                try {
                    wxMpService.getTemplateMsgService().sendTemplateMsg(wxMpTemplateMessage);
                } catch (Exception e) {
                    log.info("WeChatOfficialAccountProcessor#realSend fail! param:{},e:{}",
                            JSON.toJSONString(taskInfo), Throwables.getStackTraceAsString(e));
                }
            }
            return true;
        } catch (Exception e) {
            log.error("WeChatOfficialAccountProcessor#realSend fail:{},params:{}",
                    Throwables.getStackTraceAsString(e), JSON.toJSONString(taskInfo));
        }
        return false;
    }

    /**
     * 组装发送模板信息参数
     * @param receiver 接收者
     * @param contentModel 内容模型
     * @return
     */
    private List<WxMpTemplateMessage> assembleReq(Set<String> receiver, WeChatOfficialAccountsContentModel contentModel) {
        List<WxMpTemplateMessage> wxMpTemplateMessages = new ArrayList<>(receiver.size());
        for (String openId : receiver) {
            WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                    .toUser(openId).templateId(contentModel.getTemplateId())
                    .data(getWxMpTemplateData(contentModel.getOfficialAccountParam()))
                    .url(contentModel.getUrl())
                    .miniProgram(new WxMpTemplateMessage.MiniProgram(contentModel.getMiniProgramId(), contentModel.getPath(), false))
                    .build();
            wxMpTemplateMessages.add(templateMessage);
        }
        return wxMpTemplateMessages;
    }

    /**
     * 构建模板消息参数
     * @param data 消息参数map
     * @return
     */
    private List<WxMpTemplateData> getWxMpTemplateData(Map<String, String> data) {
        List<WxMpTemplateData> templateDataList = new ArrayList<>(data.size());
        data.forEach((k, v) -> {
            WxMpTemplateData wxMpTemplateData = new WxMpTemplateData(k, v);
            if ("first".equals(k)) {
                wxMpTemplateData.setColor("#efb4a8");
            } else if ("remark".equals(k)) {
                wxMpTemplateData.setColor("#c0f3d2");
            } else {
                wxMpTemplateData.setColor("#c0cef3");
            }
            templateDataList.add(wxMpTemplateData);
        });
        return templateDataList;
    }

    @Override
    public void recall(MessageTemplate messageTemplate) {}
}

