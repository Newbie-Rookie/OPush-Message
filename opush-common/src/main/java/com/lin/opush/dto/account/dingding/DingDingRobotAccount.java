package com.lin.opush.dto.account.dingding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 钉钉自定义机器人账号信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DingDingRobotAccount {
    /**
     * 密钥
     */
    private String secret;
    /**
     * 自定义群机器人中的webhook
     */
    private String webhook;
}
