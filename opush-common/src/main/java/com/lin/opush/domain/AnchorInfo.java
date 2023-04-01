package com.lin.opush.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 埋点信息【多用户】
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnchorInfo {
    /**
     * 发送用户
     */
    private Set<String> ids;
    /**
     * 具体点位
     */
    private Integer state;
    /**
     * 业务Id(数据追踪使用)
     * 生成逻辑：com.lin.opush.utils.TaskInfoUtils#generateBusinessId(java.lang.Long, java.lang.Integer)
     */
    private Long businessId;
    /**
     * 日志生成时间
     */
    private Long logTimestamp;
    /**
     * 创建者
     */
    private String creator;
}
