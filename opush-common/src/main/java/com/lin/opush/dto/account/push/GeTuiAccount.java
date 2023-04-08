package com.lin.opush.dto.account.push;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建个推账号时的元信息
 * （在调用个推的api时需要用到部分的参数）
 * https://docs.getui.com/getui/start/devcenter/
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeTuiAccount {
    /**
     * 账号信息
     */
    private String appId;
    private String appKey;
    private String masterSecret;
}
