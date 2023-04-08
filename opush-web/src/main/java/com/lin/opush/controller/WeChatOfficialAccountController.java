package com.lin.opush.controller;

import cn.hutool.core.util.StrUtil;
import com.google.common.base.Throwables;
import com.lin.opush.constants.CommonConstant;
import com.lin.opush.constants.WeChatOfficialAccountParamConstant;
import com.lin.opush.enums.RespStatusEnum;
import com.lin.opush.exception.CommonException;
import com.lin.opush.utils.AccountUtils;
import com.lin.opush.utils.Convert4Amis;
import com.lin.opush.vo.amis.CommonAmisVo;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 微信服务号
 */
@Slf4j
@RestController
@RequestMapping("/officialAccount")
public class WeChatOfficialAccountController {
    /**
     * 渠道账号工具类
     */
    @Autowired
    private AccountUtils accountUtils;

    /**
     * 根据渠道账号Id获取渠道配置，进而发生Http请求获取服务号模板消息的模板信息列表
     * @param id 渠道账号Id
     * @return 服务号模板消息的模板信息列表【模板标题和模板Id】
     */
    @GetMapping("/template/list")
    public List<CommonAmisVo> queryList(Long id) {
        try {
            List<CommonAmisVo> result = new ArrayList<>();
            // 微信服务号账号配置
            WxMpService wxMpService = accountUtils.getAccountById(id, WxMpService.class);
            if (!Objects.isNull(wxMpService)) {
                // 获取模板消息配置对象，进而获取服务号帐号的个人模板列表
                List<WxMpTemplate> allPrivateTemplate = wxMpService.getTemplateMsgService()
                                                                    .getAllPrivateTemplate();
                for (WxMpTemplate wxMpTemplate : allPrivateTemplate) {
                    // 模板标题和模板Id
                    CommonAmisVo commonAmisVo = CommonAmisVo.builder()
                                                .label(wxMpTemplate.getTitle())
                                                .value(wxMpTemplate.getTemplateId()).build();
                    result.add(commonAmisVo);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("WeChatOfficialAccountController#queryList fail:{}",
                                    Throwables.getStackTraceAsString(e));
            throw new CommonException(RespStatusEnum.SERVICE_ERROR);
        }
    }

    /**
     * 根据服务号账号Id和模板Id获取对应模板详细信息【关键词】
     * @param id 渠道账号Id
     * @param wxTemplateId 模板Id
     * @return 模板Id对应模板详细信息【关键词】
     */
    @PostMapping("/detailTemplate")
    public CommonAmisVo queryDetailList(Long id, String wxTemplateId) {
        if (Objects.isNull(id) || StrUtil.isBlank(wxTemplateId)) {
            return null;
        }
        try {
            // 获取服务号配置
            WxMpService wxMpService = accountUtils.getAccountById(id, WxMpService.class);
            List<WxMpTemplate> allPrivateTemplate = null;
            if (!Objects.isNull(wxMpService)) {
                // 获取模板列表
                allPrivateTemplate = wxMpService.getTemplateMsgService().getAllPrivateTemplate();
            }
            // 根据模板Id获取模板列表中对应模板的详细信息
            return Convert4Amis.getWxMpTemplateParam(wxTemplateId, allPrivateTemplate);
        } catch (Exception e) {
            log.error("WeChatOfficialAccountController#queryDetailList fail:{}",
                                            Throwables.getStackTraceAsString(e));
            throw new CommonException(RespStatusEnum.SERVICE_ERROR);
        }
    }

    /**
     * 接收微信的事件消息【验证是否为微信消息】
     * https://developers.weixin.qq.com/doc/offiaccount/Basic_Information/Access_Overview.html
     * 【给微信服务号接入使用】
     * @return
     */
    @RequestMapping(value = "/receipt", produces = {CommonConstant.CONTENT_TYPE_XML})
    public String receiptMessage(HttpServletRequest request) {
        try {
            // 微信加密签名
            String signature = request.getParameter(WeChatOfficialAccountParamConstant.SIGNATURE);
            // 时间戳
            String timestamp = request.getParameter(WeChatOfficialAccountParamConstant.TIMESTAMP);
            // 随机数
            String nonce = request.getParameter(WeChatOfficialAccountParamConstant.NONCE);
            // 随机字符串
            String echoStr = request.getParameter(WeChatOfficialAccountParamConstant.ECHO_STR);
            // echoStr != null，说明只是微信调试的请求
            if (StrUtil.isNotBlank(echoStr)) {
                return echoStr;
            }
            return RespStatusEnum.SUCCESS.getMsg();

//            // 验证消息是否来自微信服务器
//            if (!wxMpService.checkSignature(timestamp, nonce, signature)) {
//                return RespStatusEnum.CLIENT_BAD_PARAMETERS.getMsg();
//            }

//            // 接收消息和事件【加密则接收需解密，回复事件需加密】
//            String encryptType = StrUtil.isBlank(request.getParameter(
//                    WeChatOfficialAccountParamConstant.ENCRYPT_TYPE)) ?
//                    WeChatOfficialAccountParamConstant.RAW :
//                    request.getParameter(WeChatOfficialAccountParamConstant.ENCRYPT_TYPE);
//            if (WeChatOfficialAccountParamConstant.RAW.equals(encryptType)) {
//                WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(request.getInputStream());
//                log.info("raw inMessage:{}", JSON.toJSONString(inMessage));
//                WxMpXmlOutMessage outMessage = configService.getWxMpMessageRouter().route(inMessage);
//                return outMessage.toXml();
//            } else if (WeChatOfficialAccountParamConstant.AES.equals(encryptType)) {
//                String msgSignature = request.getParameter(WeChatOfficialAccountParamConstant.MSG_SIGNATURE);
//                WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(
//                                                    request.getInputStream(),
//                                                    configService.getConfig(),
//                                                    timestamp, nonce, msgSignature);
//                log.info("aes inMessage:{}", JSON.toJSONString(inMessage));
//                WxMpXmlOutMessage outMessage = configService.getWxMpMessageRouter().route(inMessage);
//                return outMessage.toEncryptedXml(configService.getConfig());
//            }
//            return RespStatusEnum.SUCCESS.getMsg();
        } catch (Exception e) {
            log.error("WeChatOfficialAccountController#receiptMessage fail:{}", Throwables.getStackTraceAsString(e));
            return RespStatusEnum.SERVICE_ERROR.getMsg();
        }
    }
}
