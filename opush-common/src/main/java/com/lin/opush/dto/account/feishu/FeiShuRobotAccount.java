package com.lin.opush.dto.account.feishu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 飞书机器人账号信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeiShuRobotAccount {
    /**
     * 自定义群机器人中的 webhook
     */
    private String webhook;
}
