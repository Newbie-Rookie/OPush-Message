package com.lin.opush.vo.amis;

import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.scene.layout.Background;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.util.List;

/**
 * 图表的Vo
 * https://aisuda.bce.baidu.com/amis/zh-CN/components/chart
 * https://www.runoob.com/echarts/echarts-setup.html
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EchartsDataVo {
    /**
     * title 标题
     */
    @JsonProperty
    private TitleVO title;
    /**
     * tooltip 提示框
     */
    @JsonProperty
    private TooltipVO tooltip;
    /**
     * legend 图例
     */
    @JsonProperty
    private LegendVO legend;
    /**
     * xAxis x轴
     */
    @JsonProperty
    private XaxisVO xAxis;
    /**
     * yAxis y轴
     */
    @JsonProperty
    private YaxisVO yAxis;
    /**
     * series 系列列表
     * 每个系列通过type决定自己的图表类型
     */
    @JsonProperty
    private List<SeriesVO> series;

    /**
     * 标题
     */
    @Data
    @Builder
    public static class TitleVO {
        /**
         * 标题文本
         */
        private String text;
        /**
         * 标题到左侧的距离
         */
        private String left;
        /**
         * 文本风格
         */
        @JsonProperty
        private TextStyleVO textStyle;
    }

    /**
     * 提示框
     */
    @Data
    @Builder
    public static class TooltipVO {
        /**
         * 颜色
         */
        private String color;
    }

    /**
     * 图例
     */
    @Data
    @Builder
    public static class LegendVO {
        /**
         * 图例与右侧的距离
         */
        private String right;
        /**
         * 图例文本
         */
        private List<String> data;
    }

    /**
     * x轴
     */
    @Data
    @Builder
    public static class XaxisVO {
        /**
         * x轴类型【value数值轴[连续数据]、category类目轴[离散数据]、time时间轴、log对数轴】
         */
        private String type;
        /**
         * x轴名称
         */
        private String name;
        /**
         * x轴上的数据
         */
        private List<String> data;
        /**
         * 坐标轴名称文本风格
         */
        @JsonProperty
        private TextStyleVO nameTextStyle;
        /**
         * 坐标轴文本风格
         */
        @JsonProperty
        private TextStyleVO axisLabel;
    }

    /**
     * y轴
     */
    @Data
    @Builder
    public static class YaxisVO {
        /**
         * y轴类型【value数值轴、category类目轴、time时间轴、log对数轴】
         */
        private String type;
        /**
         * y轴名称
         */
        private String name;
        /**
         * 坐标轴名称文本风格
         */
        @JsonProperty
        private TextStyleVO nameTextStyle;
        /**
         * 坐标轴文本风格
         */
        @JsonProperty
        private TextStyleVO axisLabel;
    }

    /**
     * 系列列表
     */
    @Data
    @Builder
    public static class SeriesVO {
        /**
         * name
         */
        private String name;
        /**
         * 图表类型【line为折线图、bar为柱状图...】
         */
        private String type;
        /**
         * data
         */
        private List<Integer> data;
        /**
         * 是否显示柱条的背景色
         */
        private Boolean showBackground;
        /**
         * 柱条背景样式
         */
        @JsonProperty
        private BackgroundStyleVO backgroundStyle;
        /**
         * 柱条本体样式
         */
        @JsonProperty
        private BackgroundStyleVO itemStyle;
    }

    /**
     * 文本风格
     */
    @Data
    @Builder
    public static class TextStyleVO{
        /**
         * 文本颜色
         */
        private String color;
        /**
         * 文本粗细
         */
        private Integer fontWeight;
        /**
         * 文本大小
         */
        private Integer fontSize;
    }

    /**
     * 柱条样式
     */
    @Data
    @Builder
    public static class BackgroundStyleVO{
        /**
         * 柱条的颜色
         */
        private String color;
        /**
         * 柱条的描边颜色
         */
        private String borderColor;
        /**
         * 柱条的描边宽度
         */
        private Integer borderWidth;
        /**
         * 圆角半径
         */
        private List<Integer> borderRadius;
        /**
         * 阴影的模糊大小
         */
        private Integer shadowBlur;
        /**
         * 阴影颜色
         */
        private String shadowColor;
        /**
         * 阴影水平方向上的偏移距离
         */
        private Integer shadowOffsetX;
        /**
         * 阴影垂直方向上的偏移距离
         */
        private Integer shadowOffsetY;
        /**
         * 图形透明度
         */
        private Integer opacity;
    }
}
