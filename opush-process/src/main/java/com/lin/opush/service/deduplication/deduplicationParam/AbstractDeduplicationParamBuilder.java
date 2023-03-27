package com.lin.opush.service.deduplication.deduplicationParam;

import com.alibaba.fastjson.JSONObject;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.service.deduplication.DeduplicationParam;
import com.lin.opush.service.deduplication.ParamAndServiceHolder;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * 去重参数构建器抽象类
 */
public abstract class AbstractDeduplicationParamBuilder implements DeduplicationParamBuilder {
    /**
     * 去重类型编码值
     */
    protected Integer deduplicationTypeCode;

    /**
     * 去重参数和去重服务持有者
     */
    @Autowired
    private ParamAndServiceHolder paramAndServiceHolder;

    /**
     * 将去重参数交由paramAndServiceHolder管理
     */
    @PostConstruct
    public void init() {
        paramAndServiceHolder.putDeduplicationParamBuilder(deduplicationTypeCode, this);
    }

    /**
     * 根据去重类型编码值获取对应去重规则构建去重参数
     * @param deduplicationTypeCode 去重类型编码值
     * @param deduplicationRule     所有去重规则（JSON字符串）
     * @param taskInfo              任务信息
     * @return 去重参数
     */
    public DeduplicationParam getDeduplicationParam(Integer deduplicationTypeCode, String deduplicationRule, TaskInfo taskInfo) {
        // 所有去重规则对应JSON字符串转换为JSON对象
        JSONObject object = JSONObject.parseObject(deduplicationRule);
        if (Objects.isNull(object)) {
            return null;
        }
        // 获取去重类型编码值对应的去重规则并转换为去重参数对象
        DeduplicationParam deduplicationParam = JSONObject.parseObject(object.getString(DEDUPLICATION_CONFIG_PREFIX + deduplicationTypeCode), DeduplicationParam.class);
        if (Objects.isNull(deduplicationParam)) {
            return null;
        }
        deduplicationParam.setTaskInfo(taskInfo);
        return deduplicationParam;
    }
}
