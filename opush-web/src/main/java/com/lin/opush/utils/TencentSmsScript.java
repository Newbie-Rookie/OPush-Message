package com.lin.opush.utils;

import cn.hutool.core.util.IdUtil;
import com.lin.opush.dto.account.sms.TencentSmsAccount;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 腾讯云短信服务
 */
@Slf4j
public class TencentSmsScript {
    public static void send(TencentSmsAccount tencentSmsAccount, String phone, String code) {
        try{
            // 初始化
            Credential cred = new Credential(tencentSmsAccount.getSecretId(), tencentSmsAccount.getSecretKey());
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(tencentSmsAccount.getUrl());
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            SmsClient client = new SmsClient(cred, tencentSmsAccount.getRegion(), clientProfile);
            // 组装入参
            SendSmsRequest req = new SendSmsRequest();
            String[] phoneNumberSet1 = new String[]{phone};
            req.setPhoneNumberSet(phoneNumberSet1);
            req.setSmsSdkAppId(tencentSmsAccount.getSmsSdkAppId());
            req.setSignName(tencentSmsAccount.getSignName());
            req.setTemplateId(tencentSmsAccount.getTemplateId());
            String[] templateParamSet1 = {code};
            req.setTemplateParamSet(templateParamSet1);
            req.setSessionContext(IdUtil.fastSimpleUUID());
            // 发送
            SendSmsResponse response = client.SendSms(req);
        } catch (TencentCloudSDKException e) {
            System.out.println(e.toString());
        }
    }
}
