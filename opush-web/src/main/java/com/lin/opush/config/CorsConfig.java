package com.lin.opush.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 */
@Configuration
public class CorsConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        // 存储request与跨域配置信息的容器，基于url的映射
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 设置允许跨域的路径
        source.registerCorsConfiguration("/**", corsConfig());
        // 添加CorsFilter过滤器并设置为最高优先级
        FilterRegistrationBean<CorsFilter> filterRegistrationBean = new FilterRegistrationBean<>(new CorsFilter(source));
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }

    /**
     * 跨域配置
     * @return
     */
    private CorsConfiguration corsConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 允许跨域访问的域名（所有）
        corsConfiguration.addAllowedOriginPattern("*");
        // corsConfiguration.addAllowedOrigin("不能使用*，需指明");
        // 是否允许请求携带验证信息（token、cookie）
        corsConfiguration.setAllowCredentials(true);
        // 允许的请求方式（所有）
        corsConfiguration.addAllowedMethod("*");
        // 允许的请求头（所有）
        corsConfiguration.addAllowedHeader("*");
        // 设置跨域语序时间
        corsConfiguration.setMaxAge(3600L);
        return corsConfiguration;
    }

    /**
     * 跨域配置过滤器
     * @return
     */
//    @Bean
//    public CorsFilter corsFilter() {
//        // 存储request与跨域配置信息的容器，基于url的映射
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        // 设置允许跨域的路径
//        source.registerCorsConfiguration("/**", corsConfig());
//        return new CorsFilter(source);
//    }
}
