package com.lin.opush.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.hutool.core.util.StrUtil;
import com.google.common.base.Throwables;
import com.lin.opush.enums.RespStatusEnum;
import com.lin.opush.exception.CommonException;
import com.lin.opush.utils.AccountUtils;
import com.lin.opush.utils.Convert4Amis;
import com.lin.opush.vo.amis.CommonAmisVo;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.subscribemsg.TemplateInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 微信小程序
 */
@Slf4j
@RestController
@RequestMapping("/miniProgram")
public class WeChatMiniProgramController {
    /**
     * 渠道账号工具类
     */
    @Autowired
    private AccountUtils accountUtils;

    /**
     * 根据渠道账号Id获取渠道配置，进而发生Http请求获取小程序订阅消息的模板信息列表
     * @param id 渠道账号Id
     * @return 小程序订阅消息的模板信息列表【模板名和模板Id】
     */
    @GetMapping("/template/list")
    public List<CommonAmisVo> queryList(Long id) {
        try {
            List<CommonAmisVo> result = new ArrayList<>();
            // 微信小程序账号配置
            WxMaService wxMaService = accountUtils.getAccountById(id, WxMaService.class);
            if(!Objects.isNull(wxMaService)){
                // 获取订阅消息配置对象，进而获取小程序帐号的个人模板列表
                List<TemplateInfo> templateList = wxMaService.getSubscribeService().getTemplateList();
                for (TemplateInfo templateInfo : templateList) {
                    // 模板名和模板ID
                    CommonAmisVo commonAmisVo = CommonAmisVo.builder()
                                                .label(templateInfo.getTitle())
                                                .value(templateInfo.getPriTmplId()).build();
                    result.add(commonAmisVo);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("WeChatMiniProgramController#queryList fail:{}", Throwables.getStackTraceAsString(e));
            throw new CommonException(RespStatusEnum.SERVICE_ERROR);
        }
    }

    /**
     * 根据小程序账号Id和模板Id获取对应模板详细信息【关键词】
     * @param id 渠道账号Id
     * @param wxTemplateId 模板Id
     * @return 模板Id对应模板详细信息【关键词】
     */
    @PostMapping("/detailTemplate")
    public CommonAmisVo queryDetailList(Long id, String wxTemplateId) {
        if (Objects.isNull(id)  || StrUtil.isBlank(wxTemplateId)) {
            return null;
        }
        try {
            // 获取小程序配置
            WxMaService wxMaService = accountUtils.getAccountById(id, WxMaService.class);
            List<TemplateInfo> templateList = null;
            if (!Objects.isNull(wxMaService)) {
                // 获取模板列表
                templateList = wxMaService.getSubscribeService().getTemplateList();
            }
            // 根据模板Id获取模板列表中对应模板的详细信息
            return Convert4Amis.getWxMaTemplateParam(wxTemplateId, templateList);
        } catch (Exception e) {
            log.error("WeChatMiniProgramController#queryDetailList fail:{}",
                                        Throwables.getStackTraceAsString(e));
            throw new CommonException(RespStatusEnum.SERVICE_ERROR);
        }
    }

//    /**
//     * 微信小程序登录凭证校验
//     * 【临时给小程序登录使用，正常消息推送平台不会有此接口】
//     * @return openId
//     */
//    @GetMapping("/sync/openid")
//    public String syncOpenId(String code, String appId, String secret) {
//        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" +
//                appId + "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";
//        return HttpUtil.get(url);
//    }
}
