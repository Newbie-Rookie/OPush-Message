package com.lin.opush.enums;

import com.lin.opush.dto.model.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 发送渠道类型
 */
@Getter
@ToString
@AllArgsConstructor
public enum ChannelType {
    /**
     * IM(站内信)  -- 未实现该渠道
     */
    IM(10, "IM「站内信」", ImContentModel.class, "im"),
    /**
     * push(通知栏) --安卓 已接入 个推
     */
    PUSH(20, "push「通知栏」", PushContentModel.class,  "push"),
    /**
     * sms(短信)  -- 腾讯云、阿里云、UniSMS
     */
    SMS(30, "sms「短信」", SmsContentModel.class, "sms"),
    /**
     * email(邮件) -- QQ、163邮箱
     * accountClass为null，使用hutool内置的MailAccount
     */
    EMAIL(40, "email「邮件」", EmailContentModel.class, "email"),
    /**
     * officialAccounts(微信服务号) -- 官方测试账号
     */
    OFFICIAL_ACCOUNT(50, "wechatOfficialAccounts「微信服务号」", WeChatOfficialAccountsContentModel.class, "wechat_official_accounts"),
    /**
     * miniProgram(微信小程序)
     */
    MINI_PROGRAM(60, "wechatMiniProgram「微信小程序」", WeChatMiniProgramContentModel.class,"wechat_mini_program"),
    /**
     * enterpriseWeChat(企业微信)
     */
    ENTERPRISE_WE_CHAT(70, "enterpriseWeChat「企业微信」", EnterpriseWeChatContentModel.class, "enterprise_wechat"),
    /**
     * dingDingRobot(钉钉机器人)
     */
    DING_DING_ROBOT(80, "dingDingRobot「钉钉机器人」", DingDingRobotContentModel.class,  "dingding_robot"),
    /**
     * dingDingWorkNotice(钉钉工作通知)
     */
    DING_DING_WORK_NOTICE(90, "dingDingWorkInform「钉钉工作通知」", DingDingWorkInformContentModel.class, "dingding_work_inform"),
    /**
     * enterpriseWeChat(企业微信机器人)
     */
    ENTERPRISE_WE_CHAT_ROBOT(100, "enterpriseWeChatRobot「企业微信机器人」", EnterpriseWeChatRobotContentModel.class, "enterprise_wechat_robot"),
    /**
     * feiShuRoot(飞书机器人)
     */
    FEI_SHU_ROBOT(110, "feiShuRobot「飞书机器人」", FeiShuRobotContentModel.class, "fei_shu_robot"),
    /**
     * alipayMiniProgram(支付宝小程序)
     */
    ALIPAY_MINI_PROGRAM(120, "alipayMiniProgram「支付宝小程序」", AlipayMiniProgramContentModel.class, "alipay_mini_program"),
    ;

    /**
     * 渠道编码值
     */
    private final Integer code;

    /**
     * 渠道描述
     */
    private final String description;

    /**
     * 发送渠道的内容模型Class
     */
    private final Class<? extends ContentModel> contentModelClass;

    /**
     * 渠道英文标识
     */
    private final String codeEn;

    /**
     * 通过code获取发送内容模型对应class
     * @param code
     * @return
     */
    public static Class<? extends ContentModel> getChanelModelClassByCode(Integer code) {
        ChannelType[] values = values();
        for (ChannelType value : values) {
            if (value.getCode().equals(code)) {
                return value.getContentModelClass();
            }
        }
        return null;
    }

    /**
     * 通过code获取enum
     * @param code
     * @return
     */
    public static ChannelType getEnumByCode(Integer code) {
        ChannelType[] values = values();
        for (ChannelType value : values) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
