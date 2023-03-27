package com.lin.opush.dto.account.sms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 腾讯云短信参数
 * 账号参数示例：
 * {
 * 	"url":"sms.tencentcloudapi.com",
 * 	"region":"ap-guangzhou",
 * 	"secretId":"AKIDpW6Z1KUXP6xxxxxxdDhQ1Sly136BEiik",
 * 	"secretKey":"xxxxxxoAQZdWFtV6fDfwS3770qxxxxxx",
 * 	"smsSdkAppId":"14xxxxxx68",
 * 	"templateId":"1698254",
 * 	"signName":"OPush公众号",
 * 	"supplierId":10,
 * 	"supplierName":"腾讯云",
 * 	"scriptName":"TencentSmsScript"
 * }
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TencentSmsAccount extends SmsAccount {
    /**
     * api相关
     */
    private String url;
    private String region;

    /**
     * 账号相关
     */
    private String secretId;
    private String secretKey;
    private String smsSdkAppId;
    private String templateId;
    private String signName;
}
