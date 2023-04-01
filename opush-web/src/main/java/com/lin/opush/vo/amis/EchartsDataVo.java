package com.lin.opush.vo.amis;

import com.fasterxml.jackson.annotation.JsonProperty;
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
         * 标题与左侧的距离
         */
        private String left;
        /**
         * 标题文本
         */
        private String text;
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
    }
}
