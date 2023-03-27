package com.lin.opush.domain.sms;

import lombok.*;

/**
 * 登录用户
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginUser {
    /**
     * 手机号
     */
    private String phone;
    /**
     * 验证码
     */
    private String code;
}
