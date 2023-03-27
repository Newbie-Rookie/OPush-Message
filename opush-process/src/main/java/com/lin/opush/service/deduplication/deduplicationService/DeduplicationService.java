package com.lin.opush.service.deduplication.deduplicationService;

import com.lin.opush.service.deduplication.DeduplicationParam;

/**
 * 去重服务接口（统一抽象为X时间段内达到了Y阈值）
 * 目前支持：
 *      (1) N分钟相同用户相同内容达到N次去重
 *      (2) 一天内相同用户N次相同渠道频次去重
 */
public interface DeduplicationService {
    /**
     * 去重方法
     * @param deduplicationParam 去重参数
     */
    void deduplication(DeduplicationParam deduplicationParam);
}
