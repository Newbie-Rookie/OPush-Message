package com.lin.opush.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 发送的消息类型（通知类、营销类、验证码）
 */
@Getter
@ToString
@AllArgsConstructor
public enum MessageType {
    /**
     * 通知类消息
     */
    INFORM(10, "通知类消息", "inform"),
    /**
     * 营销类消息
     */
    MARKETING(20, "营销类消息", "marketing"),
    /**
     * 验证码消息
     */
    VERIFICATION_CODE(30, "验证码消息", "verification_code");

    /**
     * 编码值
     */
    private final Integer code;
    /**
     * 描述
     */
    private final String description;
    /**
     * 英文标识
     */
    private final String codeEn;

    /**
     * 通过code获取enum
     * @param code 编码
     * @return
     */
    public static MessageType getEnumByCode(Integer code) {
        MessageType[] values = values();
        for (MessageType value : values) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
