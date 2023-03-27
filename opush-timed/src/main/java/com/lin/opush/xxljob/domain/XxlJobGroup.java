package com.lin.opush.xxljob.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.*;

/**
 * 执行器【组】信息
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class XxlJobGroup {
    /**
     * 执行器主键id
     */
    private int id;
    /**
     * 执行器appname【执行器注册分组依据，为空则关闭自动注册】
     */
    private String appname;
    /**
     * 执行器名称
     */
    private String title;
    /**
     * 执行器注册方式【0：自动注册，1：手动录入】
     */
    private int addressType;
    /**
     * 执行器地址列表
     * 【执行器注册方式为手动录入时可用，多个地址用逗号分隔】
     */
    private String addressList;
    /**
     * 执行器信息更新时间
     */
    private Date updateTime;
    /**
     * 执行器注册地址列表
     * 【执行器注册方式为手动录入时可用，将addressList【String】转换registryList【List】】
     */
    private List<String> registryList;

    /**
     * 获取执行器注册地址列表
     * 将执行器注册地址列表从String【逗号分隔】转为List
     * @return 执行器注册地址列表
     */
    public List<String> getRegistryList() {
        if (Objects.nonNull(addressList) && addressList.trim().length() > 0) {
            registryList = new ArrayList<>(Arrays.asList(addressList.split(",")));
        }
        return registryList;
    }
}
