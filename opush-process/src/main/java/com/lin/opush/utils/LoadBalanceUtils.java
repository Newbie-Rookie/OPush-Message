package com.lin.opush.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.lin.opush.constants.CommonConstant;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.domain.sms.MessageTypeConfig;
import com.lin.opush.dto.account.email.EmailAccount;
import com.lin.opush.dto.account.sms.SmsAccount;
import com.lin.opush.enums.ChannelType;
import com.lin.opush.service.ConfigService;
import com.lin.opush.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 流量负载工具类【暂时只支持短信、邮件进行多渠道账号流量负载】
 */
@Configuration
public class LoadBalanceUtils {
    /**
     * 配置服务
     */
    @Autowired
    private ConfigService config;

    /**
     * 渠道账号工具类
     */
    @Autowired
    private AccountUtils accountUtils;

    /**
     * 系统动态流量配置【目前暂时支持短信和硬件动态流量配置，此时sendAccount = 0】
     */
    private static final Long AUTO_FLOW_RULE = 0L;

    /**
     * 短信不同消息类型对应动态流量配置key
     */
    private static final String SMS_AUTO_FLOW_KEY = "messageTypeSmsConfig";

    /**
     * 邮件不同消息类型对应动态流量配置key
     */
    private static final String EMAIL_AUTO_FLOW_KEY = "messageTypeEmailConfig";

    /**
     * 短信、邮件不同消息类型对应动态流量配置前缀
     */
    private static final String AUTO_FLOW_KEY_PREFIX = "message_type_";

    /**
     * 获取渠道账号流量配置【短信、邮件的不同消息类型】
     *      若模板指定具体的明确账号，则优先发其账号【此时sendAccount不为0】
     *      否则走到系统动态流量配置【暂支持短信、邮件】
     * @param taskInfo 任务信息
     * @return 渠道账号
     */
    public List<MessageTypeConfig> getMessageTypeConfig(TaskInfo taskInfo) {
        // 是否为邮件任务
        boolean isSMS = (ChannelType.getEnumByCode(taskInfo.getSendChannel()) == ChannelType.SMS);
        // 判断是否走系统流量负载
        if (!taskInfo.getSendAccount().equals(AUTO_FLOW_RULE)) {
            // 短信/邮件渠道账号流量配置【设置短信/邮件渠道的该消息类型流量为100】
            MessageTypeConfig messageTypeConfig = MessageTypeConfig.builder().sendAccount(taskInfo.getSendAccount()).weights(100).build();
            if (isSMS) {
                // 获取短信渠道账号设置脚本名
                SmsAccount smsAccount = accountUtils.getAccountById(taskInfo.getSendAccount(), SmsAccount.class);
                messageTypeConfig.setScriptName(smsAccount.getScriptName());
            } else {
                // 获取邮件渠道账号设置渠道商名
                EmailAccount emailAccount = accountUtils.getAccountById(taskInfo.getSendAccount(), EmailAccount.class);
                messageTypeConfig.setSupplierName(emailAccount.getSupplierName());
            }
            return Arrays.asList(messageTypeConfig);
        }
        // 读取本地配置的对应消息类型【通知、营销、验证码】的短信/邮件流量配置
        String property = isSMS ?
                            config.getProperty(SMS_AUTO_FLOW_KEY, CommonConstant.EMPTY_VALUE_JSON_ARRAY) :
                            config.getProperty(EMAIL_AUTO_FLOW_KEY, CommonConstant.EMPTY_VALUE_JSON_ARRAY);
        JSONArray jsonArray = JSON.parseArray(property);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONArray array = jsonArray.getJSONObject(i).getJSONArray(AUTO_FLOW_KEY_PREFIX + taskInfo.getMsgType());
            if (CollUtil.isNotEmpty(array)) {
                return JSON.parseArray(JSON.toJSONString(array), MessageTypeConfig.class);
            }
        }
        return new ArrayList<>();
    }

    /**
     * 流量负载（暂时支持短信、邮件多个渠道账号流量负载）
     *      根据配置的权重优先走某个账号，并取出一个备份的
     * @param messageTypeSmsConfigs
     * @return 选中渠道商和备选渠道商配置
     */
    public MessageTypeConfig[] loadBalance(List<MessageTypeConfig> messageTypeSmsConfigs) {
        int total = 0;
        for (MessageTypeConfig channelConfig : messageTypeSmsConfigs) {
            total += channelConfig.getWeights();
        }
        // 生成一个[1,total]区间内的随机数，根据该值落在哪个区间 → 选择渠道商
        Random random = new Random();
        int index = random.nextInt(total) + 1;
        // 选中渠道商
        MessageTypeConfig pitchOnSupplier;
        // 备选渠道商
        MessageTypeConfig alterNativeSupplier;
        for (int i = 0; i < messageTypeSmsConfigs.size(); ++i) {
            if (index <= messageTypeSmsConfigs.get(i).getWeights()) {
                pitchOnSupplier = messageTypeSmsConfigs.get(i);
                // 取下一个渠道商作为备选渠道商
                int j = (i + 1) % messageTypeSmsConfigs.size();
                // 仅一个渠道商
                if (i == j) {
                    return new MessageTypeConfig[]{pitchOnSupplier};
                }
                alterNativeSupplier = messageTypeSmsConfigs.get(j);
                return new MessageTypeConfig[]{pitchOnSupplier, alterNativeSupplier};
            }
            // 删除前一个渠道商的权重(否则可能无法选中渠道商)
            index -= messageTypeSmsConfigs.get(i).getWeights();
        }
        return null;
    }
}
