package com.lin.opush.script;

import com.lin.opush.domain.SmsRecord;
import com.lin.opush.domain.sms.SmsParam;

import java.util.List;

/**
 * 短信脚本接口
 */
public interface SmsScript {
    /**
     * 发送短信
     * @param smsParam 发送短信参数
     * @return 渠道商发送接口返回值
     */
    List<SmsRecord> send(SmsParam smsParam);

    /**
     * 拉取回执
     * @param id 短信渠道账号ID
     * @return 渠道商回执接口返回值
     */
    List<SmsRecord> pull(Long id);
}
