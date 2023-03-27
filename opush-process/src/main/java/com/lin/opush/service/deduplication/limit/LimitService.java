package com.lin.opush.service.deduplication.limit;

import com.lin.opush.domain.TaskInfo;
import com.lin.opush.service.deduplication.DeduplicationParam;
import com.lin.opush.service.deduplication.deduplicationService.AbstractDeduplicationService;

import java.util.Set;

/**
 * 真正实现去重的限流服务接口
 */
public interface LimitService {
    /**
     * 去重限制
     * @param service  去重服务
     * @param taskInfo 任务信息
     * @param param    去重参数
     * @return 返回不符合条件的手机号码
     */
    Set<String> limit(AbstractDeduplicationService service, TaskInfo taskInfo, DeduplicationParam param);
}
