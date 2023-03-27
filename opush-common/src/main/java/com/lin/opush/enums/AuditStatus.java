package com.lin.opush.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 消息模板申请状态
 */
@Getter
@ToString
@AllArgsConstructor
public enum AuditStatus {
    /**
     * 10.待审核
     */
    WAIT_AUDIT(10, "待审核"),
    /**
     * 20.审核成功
     */
    AUDIT_SUCCESS(20, "审核通过"),
    /**
     * 30.被拒绝'
     */
    AUDIT_REJECT(30, "审核不通过");

    private final Integer code;
    private final String description;
}
