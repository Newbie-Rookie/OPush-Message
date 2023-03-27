package com.lin.opush.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 消息模板类型枚举
 */
@Getter
@ToString
@AllArgsConstructor
public enum MessageTemplateType {

    /**
     * 定时类的模板【后台定时调用】
     */
    TIMING(10, "定时类的模板(后台定时调用)"),
    /**
     * 实时类的模板【接口实时调用】
     */
    REALTIME(20, "实时类的模板(接口实时调用)"),
    ;

    /**
     * 编码值
     */
    private final Integer code;
    /**
     * 描述
     */
    private final String description;
}
