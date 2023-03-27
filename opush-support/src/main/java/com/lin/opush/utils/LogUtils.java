package com.lin.opush.utils;

import cn.monitor4all.logRecord.bean.LogDTO;
import cn.monitor4all.logRecord.service.CustomLogListener;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.lin.opush.domain.AnchorInfo;
import com.lin.opush.domain.LogParam;
import com.lin.opush.mq.SendMqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 所有的日志都存在
 */
@Slf4j
@Component
public class LogUtils extends CustomLogListener {
    @Autowired
    private SendMqService sendMqService;

    @Value("${opush.business.log.topic.name}")
    private String topicName;

    /**
     * @OperationLog注解所标记的方法所产生日志
     * @param logDTO
     */
    @Override
    public void createLog(LogDTO logDTO) {
        log.info(JSON.toJSONString(logDTO));
    }

    /**
     * 记录当前对象信息
     * @param logParam 日志参数
     */
    public void print(LogParam logParam) {
        logParam.setTimestamp(System.currentTimeMillis());
        log.info(JSON.toJSONString(logParam));
    }

    /**
     * 记录打点信息
     * @param anchorInfo 打点信息
     */
    public void print(AnchorInfo anchorInfo) {
        anchorInfo.setLogTimestamp(System.currentTimeMillis());
        String message = JSON.toJSONString(anchorInfo);
        log.info(message);

        try {
            sendMqService.send(topicName, message);
        } catch (Exception e) {
            log.error("LogUtils#print send mq fail! e:{},params:{}",
                                  Throwables.getStackTraceAsString(e),
                                        JSON.toJSONString(anchorInfo));
        }
    }

    /**
     * 记录当前对象信息和打点信息
     * @param logParam 日志参数
     * @param anchorInfo 打点信息
     */
    public void print(LogParam logParam, AnchorInfo anchorInfo) {
        print(anchorInfo);
        print(logParam);
    }
}
