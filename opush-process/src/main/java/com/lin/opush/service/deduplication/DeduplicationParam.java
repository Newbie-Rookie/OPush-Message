package com.lin.opush.service.deduplication;

import com.alibaba.fastjson.annotation.JSONField;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.enums.AnchorState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 去重参数（任务信息 + 去重规则【时间段 + 累计次数】 + 数据埋点【标识去重类型】）
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeduplicationParam {
    /**
     * 任务信息
     */
    private TaskInfo taskInfo;

    /**
     * 去重时间段（单位s）
     */
    @JSONField(name = "timeQuantum")
    private Long deduplicationTimeQuantum;

    /**
     * 去重所需达到的次数
     */
    @JSONField(name = "times")
    private Integer dediplicationTimes;

    /**
     * 标识属于哪种去重规则（数据埋点）
     */
    private AnchorState anchorState;
}
