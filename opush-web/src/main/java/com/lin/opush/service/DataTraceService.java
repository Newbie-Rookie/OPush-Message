package com.lin.opush.service;

import com.lin.opush.domain.SmsRecord;
import com.lin.opush.vo.DataTraceParam;
import com.lin.opush.vo.amis.EchartsDataVo;
import com.lin.opush.vo.amis.UserDataVo;
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
     * @param dataTraceParam 数据全链路追踪请求参数
     * @return 用户链路追踪VO
     */
    UserDataVo getTraceUserInfo(DataTraceParam dataTraceParam);

    /**
     * 获取全链路追踪 消息模板维度信息
     * @param dataTraceParam 数据全链路追踪请求参数
     * @return 图表VO
     */
    EchartsDataVo getTraceMessageTemplateInfo(DataTraceParam dataTraceParam);
}
