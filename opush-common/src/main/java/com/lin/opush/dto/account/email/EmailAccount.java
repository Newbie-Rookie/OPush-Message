package com.lin.opush.dto.account.email;

import cn.hutool.extra.mail.MailAccount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮件账号【使用hutool内置的MailAccount】
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailAccount extends MailAccount {
    /**
     * 标识渠道商Id
     */
    protected Integer supplierId;

    /**
     * 标识渠道商名字
     */
    protected String supplierName;
}
