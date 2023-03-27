package com.lin.opush.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 消息参数（single）
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageParam {
    /**
     * 接收者（多个则用逗号隔开，不能大于100个）
     * 【必传】
     */
    private String receiver;

    /**
     * 消息内容中的可变部分（{$title}等占位符替换）
     * 【可选】
     */
    private Map<String, String> variables;
}
