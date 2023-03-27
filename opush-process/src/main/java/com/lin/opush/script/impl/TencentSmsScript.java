package com.lin.opush.script.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.lin.opush.dao.ChannelAccountDao;
import com.lin.opush.dao.SmsRecordDao;
import com.lin.opush.domain.ChannelAccount;
import com.lin.opush.domain.SmsRecord;
import com.lin.opush.domain.sms.LoginUser;
import com.lin.opush.domain.sms.SmsParam;
import com.lin.opush.dto.account.sms.TencentSmsAccount;
import com.lin.opush.enums.SmsStatus;
import com.lin.opush.script.SmsScript;
import com.lin.opush.utils.AccountUtils;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 腾讯云短信服务
 *      (1)发送短信接入文档：https://cloud.tencent.com/document/api/382/55981
 *      (2)推荐直接使用SDK调用并使用API Explorer生成代码
 */
@Slf4j
@Component("TencentSmsScript")
public class TencentSmsScript implements SmsScript {
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
     * 短信下发记录工具类
     */
    @Autowired
    private SmsRecordDao smsRecordDao;

    /**
     * 渠道账号 Dao
     */
    @Autowired
    private ChannelAccountDao channelAccountDao;

    /**
     * 初始化发送短信客户端
     * @param account 渠道账号信息
     * @return 发短信客户端
     */
    private SmsClient initClient(TencentSmsAccount account) {
        Credential cred = new Credential(account.getSecretId(), account.getSecretKey());
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint(account.getUrl());
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        SmsClient client = new SmsClient(cred, account.getRegion(), clientProfile);
        return client;
    }

    /**
     * 组装发送短信请求
     * @param smsParam 发送短信参数
     * @param account 渠道账号信息
     * @return 发送短信请求
     */
    private SendSmsRequest assembleSendReq(SmsParam smsParam, TencentSmsAccount account) {
        SendSmsRequest req = new SendSmsRequest();
        String[] phoneNumberSet1 = smsParam.getPhones().toArray(new String[smsParam.getPhones().size() - 1]);
        req.setPhoneNumberSet(phoneNumberSet1);
        req.setSmsSdkAppId(account.getSmsSdkAppId());
        req.setSignName(account.getSignName());
        req.setTemplateId(account.getTemplateId());
        String[] templateParamSet1 = {smsParam.getContent()};
        req.setTemplateParamSet(templateParamSet1);
        req.setSessionContext(IdUtil.fastSimpleUUID());
        return req;
    }

    /**
     * 组装短信记录列表
     * @param smsParam 发送短信参数
     * @param response 发送短信返回值
     * @param account  渠道账号信息
     * @return 短信记录
     */
    private List<SmsRecord> assembleSendSmsRecord(SmsParam smsParam, SendSmsResponse response, TencentSmsAccount account) {
        if (Objects.isNull(response) || ArrayUtil.isEmpty(response.getSendStatusSet())) {
            return null;
        }
        // 短信记录列表
        List<SmsRecord> smsRecordList = new ArrayList<>();
        // 遍历所有短信下发状态
        for (SendStatus sendStatus : response.getSendStatusSet()) {
            // 腾讯返回电话号具有前缀(+86)，取巧直接翻转取后11位再翻转回来获取手机号
            String phone = new StringBuilder(new StringBuilder(sendStatus.getPhoneNumber())
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
                                    .seriesId(sendStatus.getSerialNo())
                                    .chargingNum(Math.toIntExact(sendStatus.getFee()))
                                    .status("ok".equals(sendStatus.getCode().toLowerCase()) ?
                                                        SmsStatus.SEND_SUCCESS.getCode() :
                                                        SmsStatus.SEND_FAIL.getCode())
                                    .reportContent(sendStatus.getCode())
                                    .created(Math.toIntExact(DateUtil.currentSeconds()))
                                    .updated(Math.toIntExact(DateUtil.currentSeconds()))
                                    .creator(smsParam.getCreator()).build();
            smsRecordList.add(smsRecord);
        }
        return smsRecordList;
    }

    /**
     * 组装拉取回执请求
     * @param account 渠道账号
     * @return 拉取回执请求
     */
    private PullSmsSendStatusRequest assemblePullReq(TencentSmsAccount account) {
        PullSmsSendStatusRequest req = new PullSmsSendStatusRequest();
        // 一次拉取10条
        req.setLimit(10L);
        req.setSmsSdkAppId(account.getSmsSdkAppId());
        return req;
    }

    /**
     * 组装拉取回执信息
     * @param account
     * @param resp
     * @return
     */
    private List<SmsRecord> assemblePullSmsRecord(TencentSmsAccount account, PullSmsSendStatusResponse resp) {
        List<SmsRecord> smsRecordList = new ArrayList<>();
        if (Objects.nonNull(resp) && Objects.nonNull(resp.getPullSmsSendStatusSet()) && resp.getPullSmsSendStatusSet().length > 0) {
            // 遍历拉取回执状态
            for (PullSmsSendStatus pullSmsSendStatus : resp.getPullSmsSendStatusSet()) {
                // 数据库中存在发送短信记录才组装回执【发送登录验证码短信不保存短信记录】
                if(smsRecordDao.findBySeriesIdEquals(pullSmsSendStatus.getSerialNo()) != null){
                    SmsRecord smsRecord = SmsRecord.builder()
                            // 发送时间、模板id【0】、手机号、渠道商id、渠道商名、发送内容
                            // 下发批次id、计费条数、发送状态、回执内容、创建时间、更新时间
                            .sendDate(Integer.valueOf(DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN)))
                            .messageTemplateId(0L)
                            .phone(Long.valueOf(pullSmsSendStatus.getSubscriberNumber()))
                            .supplierId(account.getSupplierId())
                            .supplierName(account.getSupplierName())
                            .msgContent("")
                            .seriesId(pullSmsSendStatus.getSerialNo())
                            .chargingNum(0)
                            .status("success".equals(pullSmsSendStatus.getReportStatus().toLowerCase()) ?
                                    SmsStatus.RECEIVE_SUCCESS.getCode() :
                                    SmsStatus.RECEIVE_FAIL.getCode())
                            .reportContent(pullSmsSendStatus.getDescription())
                            .updated(Math.toIntExact(pullSmsSendStatus.getUserReceiveTime()))
                            .created(Math.toIntExact(DateUtil.currentSeconds()))
                            .creator(smsRecordDao.findBySeriesIdEquals(pullSmsSendStatus.getSerialNo()).getCreator()).build();
                    smsRecordList.add(smsRecord);
                }
            }
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
            TencentSmsAccount account = Objects.nonNull(smsParam.getSendAccountId()) ?
                    accountUtils.getAccountById(smsParam.getSendAccountId(), TencentSmsAccount.class) :
                    accountUtils.getSmsAccountByScriptName(smsParam.getScriptName(), TencentSmsAccount.class);
            // 初始化发送短信客户端
            SmsClient client = initClient(account);
            // 组装发送短信请求
            SendSmsRequest request = assembleSendReq(smsParam, account);
            // 调用短信发送接口发送短信
            SendSmsResponse response = client.SendSms(request);
            // 组装短信记录列表
            return assembleSendSmsRecord(smsParam, response, account);
        } catch (Exception e) {
            log.error("TencentSmsScript#send fail:{},params:{}", Throwables.getStackTraceAsString(e), JSON.toJSONString(smsParam));
            return null;
        }
    }

    /**
     * 拉取回执
     * @param accountId 渠道账号Id
     * @return 短信记录列表
     */
    @Override
    public List<SmsRecord> pull(Long accountId) {
        try {
            // 渠道账号
            TencentSmsAccount account = accountUtils.getAccountById(accountId, TencentSmsAccount.class);
            // 初始化短信客户端
            SmsClient client = initClient(account);
            // 组装拉取回执请求
            PullSmsSendStatusRequest req = assemblePullReq(account);
            // 拉取回执
            PullSmsSendStatusResponse resp = client.PullSmsSendStatus(req);
            // 更新短信记录
            return assemblePullSmsRecord(account, resp);
        } catch (Exception e) {
            log.error("TencentSmsReceipt#pull fail!{}", Throwables.getStackTraceAsString(e));
            return null;
        }
    }

    /**
     * 发送用户登录验证码短信
     * @param loginUser 登录用户
     */
    public void sendLoginSms(LoginUser loginUser){
        try {
            // 获取渠道账号配置
            ChannelAccount channelAccount = channelAccountDao.findByNameEquals("腾讯云");
            TencentSmsAccount account = JSON.parseObject(channelAccount.getAccountConfig(), TencentSmsAccount.class);
            // 初始化发送短信客户端
            SmsClient client = initClient(account);
            // 组装发送短信请求
            SendSmsRequest request = new SendSmsRequest();
            request.setSmsSdkAppId(account.getSmsSdkAppId());
            request.setSignName(account.getSignName());
            request.setTemplateId(account.getTemplateId());
            request.setPhoneNumberSet(new String[]{loginUser.getPhone()});
            request.setTemplateParamSet(new String[]{loginUser.getCode()});
            request.setSessionContext(IdUtil.fastSimpleUUID());
            // 调用短信发送接口发送短信
            client.SendSms(request);
        } catch (Exception e) {
            log.error("TencentSmsReceipt#sendLoginSms fail!{}", Throwables.getStackTraceAsString(e));
        }
    }
}

