package com.lin.opush.service;

import com.lin.opush.domain.SmsRecord;
import com.lin.opush.vo.DataTraceParam;
import org.springframework.data.domain.Page;

/**
 * 数据全链路追踪服务接口
 */
public interface DataTraceService {
    /**
     * 获取短信下发记录列表
     * @param dataTraceParam 数据全链路追踪请求参数
     * @return 分页短信下发记录列表
     */
    Page<SmsRecord> querySmsDataTraceList(DataTraceParam dataTraceParam);

    /**
     * 获取全链路追踪 用户维度信息
     * @param receiver 接收者
     * @return
     */
//    UserTimeLineVo getTraceUserInfo(String receiver);

    /**
     * 获取全链路追踪 消息模板维度信息
     * @param businessId 业务ID（如果传入消息模板ID，则生成当天的业务ID）
     * @return
     */
//    EchartsVo getTraceMessageTemplateInfo(String businessId);
}
