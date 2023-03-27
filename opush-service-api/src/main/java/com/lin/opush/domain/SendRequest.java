package com.lin.opush.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 发送/撤回接口的参数
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendRequest {
    /**
     * 执行业务类型【必传】
     * 【BusinessCode → send：发送消息，recall：撤回消息】
     */
    private String code;

    /**
     * 消息模板Id【必传】
     */
    private Long messageTemplateId;

    /**
     * 消息相关的参数【执行业务类型为"send"时必传】
     */
    private MessageParam messageParam;

    /**
     * 下发者
     */
    private String creator;
}
