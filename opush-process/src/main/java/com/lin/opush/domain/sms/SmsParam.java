package com.lin.opush.domain.sms;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * 发送短信参数
 */
@Data
@Builder
public class SmsParam {
    /**
     * 消息模板Id
     */
    private Long messageTemplateId;

    /**
     * 需要发送的手机号
     */
    private Set<String> phones;

    /**
     * 发送渠道账号的id【若短信模板内指定发送渠道账号，则该字段有值】
     * 若指定发送渠道账号，则用发送渠道账号id检索，否则用scriptName检索
     */
    private Long sendAccountId;

    /**
     * 渠道账号的脚本名标识
     */
    private String scriptName;

    /**
     * 发送文案
     */
    private String content;

    /**
     * 下发者
     */
    private String creator;
}
