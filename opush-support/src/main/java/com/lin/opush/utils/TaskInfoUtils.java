package com.lin.opush.utils;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.lin.opush.constants.CommonConstant;

import java.util.Date;

/**
 * 生成消息推送的URL的工具类
 */
public class TaskInfoUtils {
    private static final int TYPE_FLAG = 1000000;
    private static final String CODE = "track_code_bid";

    /**
     * 业务id生成 → 消息模板类型【前2位】 + 消息模板id【3 ~ 8位】 + 当天日期【后8位】
     */
    public static Long generateBusinessId(Long templateId, Integer templateType) {
        Integer today = Integer.valueOf(DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN));
        return Long.valueOf(String.format("%d%s", templateType * TYPE_FLAG + templateId, today));
    }

    /**
     * 从业务id【3 ~ 8位】中切割出消息模板id
     */
    public static Long getMessageTemplateIdFromBusinessId(Long businessId) {
        return Long.valueOf(String.valueOf(businessId).substring(1, 8));
    }

    /**
     * 从业务id【后8位】中切割出日期
     */
    public static Long getDateFromBusinessId(Long businessId) {
        return Long.valueOf(String.valueOf(businessId).substring(8));
    }

    /**
     * 给url添加平台参数（用于追踪数据)
     */
    public static String generateUrl(String url, Long templateId, Integer templateType) {
        url = url.trim();
        Long businessId = generateBusinessId(templateId, templateType);
        // url拼接
        // 当url不包含?时，拼接?track_code_bid=业务id
        // 当url包含?时，拼接&track_code_bid=业务id
        if (url.indexOf(CommonConstant.QM) == -1) {
            return url + CommonConstant.QM_STRING + CODE + CommonConstant.EQUAL_STRING + businessId;
        } else {
            return url + CommonConstant.AND_STRING + CODE + CommonConstant.EQUAL_STRING + businessId;
        }
    }
}
