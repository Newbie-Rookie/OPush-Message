package com.lin.opush.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    /**
     * 手机号码
     */
    private String phone;
    /**
     * 验证码
     */
    private String code;
    /**
     * 身份验证token（登录验证）
     */
    private String token;
}
