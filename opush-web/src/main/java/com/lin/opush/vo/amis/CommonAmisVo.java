package com.lin.opush.vo.amis;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AMIS通用转化类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommonAmisVo {

    private String type;
    private String id;

    private String label;

    private String value;
    private String schemaApi;

    private String mode;
    private String name;
    private boolean fixedSize;
    private String fixedSizeClassName;
    private String frameImage;
    private String originalSrc;
    private Integer interval;

    private boolean required;
    private boolean silentPolling;

    private String size;
    private String target;

    // 表格（新增行、编辑、删除、是否可确认）
    private boolean addable;
    private boolean editable;
    private boolean removable;
    private boolean needConfirm;

    // 宽度、高度
    private String width;
    private String height;

    private String src;

    private String title;

    private String imageMode;

    private String varParam;

    private List<CommonAmisVo> body;

    private ApiDTO api;

    // 列集合
    @JSONField(name = "columns")
    private List<ColumnsDTO> columns;

    // 列对象
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class ColumnsDTO {
        // 列名
        @JSONField(name = "name")
        private String name;
        // 列标签
        @JSONField(name = "label")
        private String label;
        // 列类型
        @JSONField(name = "type")
        private String type;
        // 列提示
        @JSONField(name = "placeholder")
        private String placeholder;
        //
        @JSONField(name = "required")
        private Boolean required;
        //
        @JSONField(name = "quickEdit")
        private Boolean quickEdit;
    }

    /**
     * ApiDTO
     */
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class ApiDTO {
        /**
         * adaptor
         */
        @JSONField(name = "adaptor")
        private String adaptor;
        /**
         * adaptor
         */
        @JSONField(name = "requestAdaptor")
        private String requestAdaptor;
        /**
         * url
         */
        @JSONField(name = "url")
        private String url;
    }
}
