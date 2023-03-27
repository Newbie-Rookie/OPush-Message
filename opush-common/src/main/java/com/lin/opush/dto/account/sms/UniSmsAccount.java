package com.lin.opush.dto.account.sms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UniSMS账号信息
 * 账号参数示例：
 * {
 * 	"accessKeyId":"xxxxxxwC9ujrFaRSVY6izgxxxxxx5E33mDtx6LAHaBcxxxxxx",
 * 	"accessKeySecret":"xxxxxx7TGoWq8rr8HPsLT1Xsxxxxxx",
 * 	"templateId":"pub_verif_basic",
 * 	"signature":"OPush",
 * 	"supplierId":30,
 * 	"supplierName":"UniSMS",
 * 	"scriptName":"UniSmsScript"
 * }
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UniSmsAccount extends SmsAccount {
    /**
     * 账号相关
     */
    private String accessKeyId;
    private String accessKeySecret;
    /**
     * 模板id
     */
    private String templateId;
    /**
     * 签名
     */
    private String signature;
}
