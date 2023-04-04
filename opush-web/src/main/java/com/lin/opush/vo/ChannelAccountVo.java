package com.lin.opush.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 渠道账号列表的Vo
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChannelAccountVo {
    /**
     * 返回List列表
     */
    private List<Map<String, Object>> rows;
    /**
     * 总条数
     */
    private Long count;
}
