package com.lin.opush.constants;

/**
 * 微信服务号的参数常量
 */
public class WeChatOfficialAccountParamConstant {
    // 身份签名【验证是否来自微信服务器的消息】
    public static final String SIGNATURE = "signature";
    // token【用户在自身服务器配置】、timestamp、nonce三个参数用于验证消息来源
    public static final String TIMESTAMP = "timestamp";
    public static final String NONCE = "nonce";
    // 若为微信服务器的消息则返回该参数
    public static final String ECHO_STR = "echostr";

    // 消息加解密模式【明文模式、兼容模式、密文模式】【未加密则为空，加密则为aes】
    // 若加密则接收消息和事件时需解密，回复事件时需加密，但通过API主动调用接口时【包括调用客服消息接口发消息】，无需加密
    public static final String ENCRYPT_TYPE = "encrypt_type";
    // 明文模式
    public static final String RAW = "raw";
    // 密文模式
    public static final String AES = "aes";
    // 消息体签名【用于验证消息体的正确性】
    public static final String MSG_SIGNATURE = "msg_signature";
}
