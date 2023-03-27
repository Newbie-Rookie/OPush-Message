package com.lin.opush.script.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.apistd.uni.Uni;
import com.apistd.uni.UniResponse;
import com.apistd.uni.sms.UniMessage;
import com.apistd.uni.sms.UniSMS;
import com.google.common.base.Throwables;
import com.lin.opush.dao.ChannelAccountDao;
import com.lin.opush.dao.SmsRecordDao;
import com.lin.opush.domain.ChannelAccount;
import com.lin.opush.domain.SmsRecord;
import com.lin.opush.domain.sms.SmsParam;
import com.lin.opush.domain.sms.SmsReceipt;
import com.lin.opush.dto.account.sms.UniSmsAccount;
import com.lin.opush.enums.SmsStatus;
import com.lin.opush.script.SmsScript;
import com.lin.opush.utils.AccountUtils;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * UniSMS短信服务
 *      发送短信文档：https://unisms.apistd.com/docs/api/send
 *      拉取回执文档：https://unisms.apistd.com/docs/extension/sms-dlr
 */
@Slf4j
@Component("UniSmsScript")
public class UniSmsScript implements SmsScript {
    /**
     * 手机号位数
     */
    private static final Integer PHONE_NUM = 11;

    /**
     * 渠道账号工具类
     */
    @Autowired
    private AccountUtils accountUtils;

    /**
     * 短信下发记录 Dao
     */
    @Autowired
    private SmsRecordDao smsRecordDao;

    /**
     * 渠道账号 Dao
     */
    @Autowired
    private ChannelAccountDao channelAccountDao;

    /**
     * 该短信渠道名【UniSMS】
     */
    private static final String SMS_SUPPLIER = "UniSMS";

    /**
     * 组装发送短信请求
     * @param smsParam 发送短信参数
     * @param account 渠道账号信息
     * @return 发送短信请求
     */
    private UniMessage assembleUniMessage(SmsParam smsParam, UniSmsAccount account) {
        // 初始化
        Uni.init(account.getAccessKeyId(), account.getAccessKeySecret());
        // 设置自定义参数 (变量短信)
        Map<String, String> templateData = new HashMap();
        templateData.put("code", smsParam.getContent());
        // 构建发送短信请求
        return UniSMS.buildMessage()
                    .setTo(smsParam.getPhones().toArray(new String[smsParam.getPhones().size()]))
                    .setSignature(account.getSignature())
                    .setTemplateId(account.getTemplateId())
                    .setTemplateData(templateData);
    }

    /**
     * 组装短信记录列表
     * @param smsParam 发送短信参数
     * @param response 发送短信返回值
     * @param account  渠道账号信息
     * @return 短信记录
     */
    private List<SmsRecord> assembleSendSmsRecord(SmsParam smsParam, UniResponse response, UniSmsAccount account) {
        if (Objects.isNull(response) || "200".equals(response.status) ||
                !"0".equals(response.code) || !"success".equals(response.message.toLowerCase())) {
            return null;
        }
        // 短信发送返回的信息数组
        JSONArray messages = response.data.getJSONArray("messages");
        // 短信记录列表
        List<SmsRecord> smsRecordList = new ArrayList<>();
        // 遍历所有短信返回的信息数组
        for(int ind = 0;ind < messages.length();++ind){
            JSONObject message = messages.getJSONObject(ind);
            // UniSMS返回电话号具有前缀(+86)，取巧直接翻转取后11位再翻转回来获取手机号
            String phone = new StringBuilder(new StringBuilder((String) message.get("to"))
                                                .reverse().substring(0, PHONE_NUM)).reverse().toString();
            // 组装短信记录
            SmsRecord smsRecord = SmsRecord.builder()
                                // 发送时间、模板id、手机号、渠道商id、渠道商名字、发送内容
                                // 下发批次Id、计费条数、发送状态、回执内容、创建时间、更新时间
                                .sendDate(Integer.valueOf(DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN)))
                                .messageTemplateId(smsParam.getMessageTemplateId())
                                .phone(Long.valueOf(phone))
                                .supplierId(account.getSupplierId())
                                .supplierName(account.getSupplierName())
                                .msgContent(smsParam.getContent())
                                .seriesId(message.getString("id"))
                                .chargingNum(message.getInt("messageCount"))
                                .status("sent".equals(message.getString("status").toLowerCase()) ?
                                                                    SmsStatus.SEND_SUCCESS.getCode() :
                                                                    SmsStatus.SEND_FAIL.getCode())
                                .reportContent(message.getString("status"))
                                .created(Math.toIntExact(DateUtil.currentSeconds()))
                                .updated(Math.toIntExact(DateUtil.currentSeconds()))
                                .creator(smsParam.getCreator()).build();
            smsRecordList.add(smsRecord);
        }
        return smsRecordList;
    }

    /**
     * 发送短信
     * @param smsParam 发送短信参数
     * @return 短信记录列表
     */
    @Override
    public List<SmsRecord> send(SmsParam smsParam) {
        try {
            // 若前端指定使用系统动态流量配置，此时sendAccountId为0，使用scriptName获取渠道账号配置
            UniSmsAccount account = Objects.nonNull(smsParam.getSendAccountId()) ?
                    accountUtils.getAccountById(smsParam.getSendAccountId(), UniSmsAccount.class) :
                    accountUtils.getSmsAccountByScriptName(smsParam.getScriptName(), UniSmsAccount.class);
            // 组装发送短信请求
            UniMessage message = assembleUniMessage(smsParam, account);
            // 调用短信发送接口发送短信
            UniResponse response = message.send();
            // 组装短信记录列表
            return assembleSendSmsRecord(smsParam, response, account);
        } catch (Exception e) {
            log.error("UniSmsScript#send fail:{},params:{}", Throwables.getStackTraceAsString(e), JSON.toJSONString(smsParam));
            return null;
        }
    }

    /**
     * 暂时不使用该方式拉取短信回执
     * @param id 短信渠道账号ID
     * @return 短信记录列表
     */
    @Override
    public List<SmsRecord> pull(Long id) {
        return null;
    }

    /**
     * 保存回执
     * @param receipt UniSMS短信回执
     */
    public void saveSmsReceipt(SmsReceipt receipt) {
        try {
            // 渠道账号信息【UniSMS对应的渠道账号信息】
            ChannelAccount channelAccount = channelAccountDao.findByNameEquals(SMS_SUPPLIER);
            // 渠道账号配置
            UniSmsAccount account = accountUtils.getAccountById(channelAccount.getId(), UniSmsAccount.class);
            // 组装短信记录
            SmsRecord smsRecord = assembleSmsRecord(account, receipt);
            // 保存短信记录
            if (Objects.nonNull(smsRecord)) {
                // 持久化
                smsRecordDao.save(smsRecord);
            }
        } catch (Exception e) {
            log.error("UniSmsScript#saveSmsReceipt fail!{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 组装短信记录
     * @param account 渠道账号
     * @param receipt 消息回执
     * @return 短信记录
     */
    private SmsRecord assembleSmsRecord(UniSmsAccount account, SmsReceipt receipt) {
        SmsRecord smsRecord = null;
        // 渠道账号和回执不为空
        if (Objects.nonNull(account) && Objects.nonNull(receipt)) {
            // UniSMS发的短信回执中电话号具有前缀(+86)，取巧直接翻转取后11位再翻转回来获取手机号
            String phone = new StringBuilder(new StringBuilder(receipt.getTo())
                                                 .reverse().substring(0, PHONE_NUM)).reverse().toString();
            smsRecord = SmsRecord.builder()
                            // 发送时间、模板id【0】、手机号、渠道商id、渠道商名、发送内容
                            // 下发批次id、计费条数、发送状态、回执内容、创建时间、更新时间
                            .sendDate(Integer.valueOf(DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN)))
                            .messageTemplateId(0L)
                            .phone(Long.valueOf(phone))
                            .supplierId(account.getSupplierId())
                            .supplierName(account.getSupplierName())
                            .msgContent("")
                            .seriesId(receipt.getId())
                            .chargingNum(0)
                            .status("delivered".equals(receipt.getStatus().toLowerCase()) ?
                                                    SmsStatus.RECEIVE_SUCCESS.getCode() :
                                                    SmsStatus.RECEIVE_FAIL.getCode())
                            .reportContent(receipt.getErrorMessage() + " (" + receipt.getErrorCode() + ")")
                            .updated(Math.toIntExact(DateUtil.parse(receipt.getDoneDate()).getTime() / 1000))
                            .created(Math.toIntExact(DateUtil.currentSeconds()))
                            .creator(smsRecordDao.findBySeriesIdEquals(receipt.getId()).getCreator()).build();
        }
        return smsRecord;
    }
}
