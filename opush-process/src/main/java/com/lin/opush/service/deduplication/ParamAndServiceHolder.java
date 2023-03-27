package com.lin.opush.service.deduplication;

import com.lin.opush.service.deduplication.deduplicationParam.DeduplicationParamBuilder;
import com.lin.opush.service.deduplication.deduplicationService.DeduplicationService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 去重参数构建器具体实现类和去重服务具体实现类持有者
 */
@Service
public class ParamAndServiceHolder {
    /**
     * 存储去重类型编码值 >> 该去重类型编码值对应的去重参数构建器具体实现类
     */
    private final Map<Integer, DeduplicationParamBuilder> deduplicationParamBuilderHolder = new HashMap<>(4);

    /**
     * 存储去重类型编码值 >> 该去重类型编码值对应的去重服务具体实现类
     */
    private final Map<Integer, DeduplicationService> dediplicationServiceHolder = new HashMap<>(4);

    public DeduplicationParamBuilder getDeduplicationParamBuilder(Integer deduplicationTypeCode) {
        return deduplicationParamBuilderHolder.get(deduplicationTypeCode);
    }

    public void putDeduplicationParamBuilder(Integer deduplicationTypeCode, DeduplicationParamBuilder deduplicationParamBuilder) {
        deduplicationParamBuilderHolder.put(deduplicationTypeCode, deduplicationParamBuilder);
    }

    public DeduplicationService getDeduplicationService(Integer deduplicationTypeCode) {
        return dediplicationServiceHolder.get(deduplicationTypeCode);
    }

    public void putDeduplicationService(Integer deduplicationTypeCode, DeduplicationService deduplicationService) {
        dediplicationServiceHolder.put(deduplicationTypeCode, deduplicationService);
    }
}
