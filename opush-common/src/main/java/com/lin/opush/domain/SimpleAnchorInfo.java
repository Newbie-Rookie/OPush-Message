package com.lin.opush.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 简单的埋点信息【单用户】
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleAnchorInfo {
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
}
