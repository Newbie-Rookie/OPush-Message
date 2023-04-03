package com.lin.opush.receipt.stater.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.lin.opush.constants.CommonConstant;
import com.lin.opush.dao.ChannelAccountDao;
import com.lin.opush.dao.SmsRecordDao;
import com.lin.opush.domain.ChannelAccount;
import com.lin.opush.domain.SmsRecord;
import com.lin.opush.dto.account.sms.SmsAccount;
import com.lin.opush.enums.ChannelType;
import com.lin.opush.receipt.stater.PullReceiptStater;
import com.lin.opush.script.SmsScript;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


/**
 * 拉取短信回执信息
 */
@Component
@Slf4j
public class PullSmsReceiptStarterImpl implements PullReceiptStater {
    /**
     * 渠道账号 Dao
     */
    @Autowired
    private ChannelAccountDao channelAccountDao;

    /**
     * 自动注入SmsScript的实现类对象【TencentSmsScript...】到Map<实现类对象名,实现类对象>
     *【只有在Map的key为String类型时才有效】
     */
    @Autowired
    private Map<String, SmsScript> scriptMap;

    /**
     * 部分脚本【UniSmsScript】不使用主动拉取的方式拉取短信回执
     * 需排除的脚本
     */
    private static final String EXCLUDE_SMS_SUPPLIER = "UniSMS";

    /**
     * 短信记录 Dao
     */
    @Autowired
    private SmsRecordDao smsRecordDao;

    /**
     * 拉取回执消息并入库
     */
    @Override
    public void start() {
        try {
            // 获取短信渠道账号列表
            List<ChannelAccount> channelAccountList = channelAccountDao
                    .findAllBySendChannelEqualsAndIsDeletedEquals(ChannelType.SMS.getCode(), CommonConstant.FALSE);
            // 遍历短信渠道账号
            for (ChannelAccount channelAccount : channelAccountList) {
                // 排除部分脚本【不属于主动拉取的方式拉取回执】
                if (!EXCLUDE_SMS_SUPPLIER.equals(channelAccount.getName())) {
                    // 获取短信渠道配置
                    SmsAccount smsAccount = JSON.parseObject(channelAccount.getAccountConfig(), SmsAccount.class);
                    // 拉取回执
                    List<SmsRecord> smsRecordList = scriptMap.get(smsAccount.getScriptName()).pull(channelAccount.getId());
                    if (CollUtil.isNotEmpty(smsRecordList)) {
                        // 持久化
                        smsRecordDao.saveAll(smsRecordList);
                    }
                }
            }
        } catch (Exception e) {
            log.error("PullSmsReceiptStarterImpl#start fail:{}", Throwables.getStackTraceAsString(e));

        }
    }
}
