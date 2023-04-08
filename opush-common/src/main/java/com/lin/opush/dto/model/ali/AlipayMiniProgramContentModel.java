package com.lin.opush.dto.model.ali;

import com.lin.opush.dto.model.ContentModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 支付宝小程序订阅消息内容模型
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlipayMiniProgramContentModel extends ContentModel {
    /**
     * 模板消息发送的数据
     */
    Map<String, String> map;
}
