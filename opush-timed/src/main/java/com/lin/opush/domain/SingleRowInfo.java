package com.lin.opush.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * 单行信息类
 * 【消息模板Id + Csv文件中每行记录信息[接收者id、消息内容的可变部分]】
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SingleRowInfo implements Serializable {
    /**
     * 消息模板Id
     */
    private Long messageTemplateId;

    /**
     * 接收者id
     */
    private String receiver;

    /**
     * 消息内容中的可变部分【将{$title}等占位符替换】
     */
    private Map<String, String> params;

    /**
     * 下发者【定时任务创建者】
     */
    private String creator;
}
