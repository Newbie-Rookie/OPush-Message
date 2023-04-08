package com.lin.opush.dto.model.wechat;

import com.lin.opush.dto.model.ContentModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 微信服务号模板信息内容模型
 * https://developers.weixin.qq.com/doc/offiaccount/Message_Management/Template_Message_Interface.html
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeChatOfficialAccountsContentModel extends ContentModel {
    /**
     * 模板Id
     */
    private String templateId;
    /**
     * 模板消息发送的数据
     */
    private Map<String, String> officialAccountParam;
    /**
     * 模板消息跳转的url
     */
    private String url;
    /**
     * 模板消息跳转小程序的appid
     */
    private String miniProgramId;
    /**
     * 模板消息跳转小程序的页面路径
     */
    private String path;
}
