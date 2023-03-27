package com.lin.opush.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 数据全链路追踪请求参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataTraceParam {
    /**
     * 当前页码（>0）
     */
    @NotNull
    private Integer page = 1;

    /**
     * 当前页大小（>0, 5、10、20、50、100）
     */
    @NotNull
    private Integer perPage = 10;

    /**
     * 查看用户的链路信息
     */
    private String receiver;

    /**
     * 业务Id【数据追踪使用】
     * 生成逻辑：com.lin.opush.utils.TaskInfoUtils#generateBusinessId
     */
    private String businessId;

    /**
     * 日期时间【检索短信的条件使用】
     */
    private Long dateTime;

    /**
     * 下发者
     */
    private String creator;
}
