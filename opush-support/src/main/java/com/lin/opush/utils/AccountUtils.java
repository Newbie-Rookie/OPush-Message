package com.lin.opush.utils;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.lin.opush.dao.ChannelAccountDao;
import com.lin.opush.domain.ChannelAccount;
import com.lin.opush.dto.account.email.EmailAccount;
import com.lin.opush.dto.account.sms.SmsAccount;
import com.lin.opush.enums.ChannelType;
import com.sun.mail.util.MailSSLSocketFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Optional;

/**
 * 渠道账号工具类
 */
@Slf4j
@Configuration
public class AccountUtils {
    /**
     * 渠道账号 Dao
     */
    @Autowired
    private ChannelAccountDao channelAccountDao;

    /**
     * Redis模板
     */
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 通过脚本名匹配到对应的短信账号
     * @param scriptName 脚本名
     * @param clazz 脚本名对应的class
     * @param <T> 短信账号
     * @return
     */
    public <T> T getSmsAccountByScriptName(String scriptName, Class<T> clazz) {
        try {
            // 根据发送渠道获取所有短信渠道账号
            List<ChannelAccount> channelAccountList = channelAccountDao.findAllBySendChannelEquals(ChannelType.SMS.getCode());
            for (ChannelAccount channelAccount : channelAccountList) {
                try {
                    // 根据scriptName(脚本名称)获取对应渠道账号
                    SmsAccount smsAccount = JSON.parseObject(channelAccount.getAccountConfig(), SmsAccount.class);
                    if (smsAccount.getScriptName().equals(scriptName)) {
                        return JSON.parseObject(channelAccount.getAccountConfig(), clazz);
                    }
                } catch (Exception e) {
                    log.error("AccountUtils#getSmsAccountByScriptName parse fail! e:{},account:{}", Throwables.getStackTraceAsString(e), JSON.toJSONString(channelAccount));
                }
            }
        } catch (Exception e) {
            log.error("AccountUtils#getSmsAccountByScriptName fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        log.error("AccountUtils#getSmsAccountByScriptName not found!:{}", scriptName);
        return null;
    }

    /**
     * 通过渠道商名匹配到对应的邮件账号
     * @param supplierName 渠道商名
     * @param clazz EmailAccount.class
     * @return 邮件账号
     */
    public EmailAccount getEmailAccountBySupplierName(String supplierName, Class<EmailAccount> clazz) {
        try {
            // 根据发送渠道获取所有邮件渠道账号
            List<ChannelAccount> channelAccountList = channelAccountDao.findAllBySendChannelEquals(ChannelType.EMAIL.getCode());
            for (ChannelAccount channelAccount : channelAccountList) {
                try {
                    // 根据supplierName(渠道商名称)获取对应渠道账号
                    EmailAccount emailAccount = JSON.parseObject(channelAccount.getAccountConfig(), clazz);
                    if (emailAccount.getSupplierName().equals(supplierName)) {
                        MailSSLSocketFactory sf = new MailSSLSocketFactory();
                        // 设置SSL加密
                        sf.setTrustAllHosts(true);
                        // 检查是否使用STARTTLS安全连接、是否授权、是否使用SSL安全连接
                        emailAccount.setStarttlsEnable(emailAccount.isStarttlsEnable())
                                    .setAuth(emailAccount.isAuth())
                                    .setSslEnable(emailAccount.isSslEnable())
                                    .setCustomProperty("mail.smtp.ssl.socketFactory", sf)
                                    // SMTP超时时长、Socket超时时长
                                    .setTimeout(25000).setConnectionTimeout(25000);
                        return emailAccount;
                    }
                } catch (Exception e) {
                    log.error("AccountUtils#getEmailAccountBySupplierName parse fail! e:{},account:{}", Throwables.getStackTraceAsString(e), JSON.toJSONString(channelAccount));
                }
            }
        } catch (Exception e) {
            log.error("AccountUtils#getEmailAccountBySupplierName fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        log.error("AccountUtils#getEmailAccountBySupplierName not found!:{}", supplierName);
        return null;
    }

    /**
     * 微信小程序：返回 WxMaService
     * 微信服务号：返回 WxMpService
     * 其他渠道：返回XXXAccount账号对象
     * @param sendAccountId
     * @param clazz
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getAccountById(Long sendAccountId, Class<T> clazz) {
        try {
            Optional<ChannelAccount> optionalChannelAccount = channelAccountDao.findById(Long.valueOf(sendAccountId));
            if (optionalChannelAccount.isPresent()) {
                ChannelAccount channelAccount = optionalChannelAccount.get();
//                if (clazz.equals(WxMaService.class)) {
//                    return (T) ConcurrentHashMapUtils.computeIfAbsent(miniProgramServiceMap, channelAccount, account -> initMiniProgramService(JSON.parseObject(account.getAccountConfig(), WeChatMiniProgramAccount.class)));
//                } else if (clazz.equals(WxMpService.class)) {
//                    return (T) ConcurrentHashMapUtils.computeIfAbsent(officialAccountServiceMap, channelAccount, account -> initOfficialAccountService(JSON.parseObject(account.getAccountConfig(), WeChatOfficialAccount.class)));
//                } else {
                    return JSON.parseObject(channelAccount.getAccountConfig(), clazz);
//                }
            }
        } catch (Exception e) {
            log.error("AccountUtils#getAccountById fail! e:{}", Throwables.getStackTraceAsString(e));
        }
        return null;
    }

    /**
     * 消息的小程序/微信服务号账号
     */
//    private ConcurrentMap<ChannelAccount, WxMpService> officialAccountServiceMap = new ConcurrentHashMap<>();
//    private ConcurrentMap<ChannelAccount, WxMaService> miniProgramServiceMap = new ConcurrentHashMap<>();
//
//    @Bean
//    public RedisTemplateWxRedisOps redisTemplateWxRedisOps() {
//        return new RedisTemplateWxRedisOps(stringRedisTemplate);
//    }

    /**
     * 初始化微信服务号
     * access_token 用redis存储
     * @return
     */
//    public WxMpService initOfficialAccountService(WeChatOfficialAccount officialAccount) {
//        WxMpService wxMpService = new WxMpServiceImpl();
//        WxMpRedisConfigImpl config = new WxMpRedisConfigImpl(redisTemplateWxRedisOps(), SendAccountConstant.OFFICIAL_ACCOUNT_ACCESS_TOKEN_PREFIX);
//        config.setAppId(officialAccount.getAppId());
//        config.setSecret(officialAccount.getSecret());
//        config.setToken(officialAccount.getToken());
//        wxMpService.setWxMpConfigStorage(config);
//        return wxMpService;
//    }

    /**
     * 初始化微信小程序
     * access_token 用redis存储
     *
     * @return
     */
//    private WxMaService initMiniProgramService(WeChatMiniProgramAccount miniProgramAccount) {
//        WxMaService wxMaService = new WxMaServiceImpl();
//        WxMaRedisBetterConfigImpl config = new WxMaRedisBetterConfigImpl(redisTemplateWxRedisOps(), SendAccountConstant.MINI_PROGRAM_TOKEN_PREFIX);
//        config.setAppid(miniProgramAccount.getAppId());
//        config.setSecret(miniProgramAccount.getAppSecret());
//        wxMaService.setWxMaConfig(config);
//        return wxMaService;
//    }
}
