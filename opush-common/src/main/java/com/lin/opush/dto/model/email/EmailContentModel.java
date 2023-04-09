package com.lin.opush.dto.model.email;

import com.lin.opush.dto.model.ContentModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮件消息体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailContentModel extends ContentModel {
    /**
     * 标题
     */
    private String title;
    /**
     * 内容【支持HTML】
     */
    private String content;
    /**
     * 本地附件链接
     */
    private String localFilePath;
    /**
     * 远程附件链接
     */
    private String url;
}
