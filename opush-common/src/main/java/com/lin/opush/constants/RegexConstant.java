package com.lin.opush.constants;

/**
 * 正则表达式常量
 */
public class RegexConstant {
    // 手机号
    public static final String PHONE_REGEX = "^1(3\\d|4[5-9]|5[0-35-9]|6[567]|7[0-8]|8\\d|9[0-35-9])\\d{8}$";
    // 验证码
    public static final String CODE_REGEX = "^\\d{6}$";
    // 邮箱
    public static final String EMAIL_REGEX = "^[A-Za-z0-9-_\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    // 匹配1~6位模板ID
    public static final String MESSAGE_TEMPLATE_REGEX = "^\\d{1,6}$";
    // 匹配16位业务ID
    public static final String BUSINESSID_REGEX = "^\\d{16}$";
}
