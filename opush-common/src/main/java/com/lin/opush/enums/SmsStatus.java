package com.lin.opush.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 短信状态信息
 */
@Getter
@ToString
@AllArgsConstructor
public enum SmsStatus {
    /**
     * 调用渠道接口发送成功
     */
    SEND_SUCCESS(10, "调用渠道接口发送成功"),

    /**
     * 用户收到短信(收到渠道短信回执，状态成功)
     */
    RECEIVE_SUCCESS(20, "用户收到短信(收到渠道短信回执，状态成功)"),

    /**
     * 用户收不到短信(收到渠道短信回执，状态失败)
     */
    RECEIVE_FAIL(30, "用户收不到短信(收到渠道短信回执，状态失败)"),

    /**
     * 调用渠道接口发送失败
     */
    SEND_FAIL(40, "调用渠道接口发送失败");

    /**
     * 短信状态编码值
     */
    private final Integer code;

    /**
     * 短信状态描述信息
     */
    private final String description;

    /**
     * 根据短信状态编码值获取短信状态描述信息
     * @param code 编码值
     * @return 描述信息
     */
    public static String getDescriptionByStatus(Integer code) {
        for (SmsStatus value : SmsStatus.values()) {
            if (value.getCode().equals(code)) {
                return value.getDescription();
            }
        }
        return "";
    }
}
