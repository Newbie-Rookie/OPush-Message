package com.lin.opush.config;

import com.lin.opush.interceptor.LoginInterceptor;
import com.lin.opush.interceptor.RefreshTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * MVC配置
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /*
     * addInterceptors和addCorsMappings冲突问题：
     *      addCorsMappings添加跨域配置其实就是在请求的拦截器链的最后再加一个拦截器，
     *      但有些请求会提前被addInterceptors中配置的拦截器拦截，导致跨域配置失效，前端报跨域异常
     *
     * 两个解决方案：
     *      (1)提高CorsInterceptor的优先级，所以就不能借助于spring的addCorsMappings方法，
     *          只能手动配置一个CrosInterceptor，且优先级大于其它的interceptor
     *      (2)过滤器会在拦截器链执行前就对请求进行处理，添加一个进行跨域处理的过滤器CorsFilter，
     *          Spring为我们提供了这样一个过滤器CorsFilter
     */
    /**
     * 登录拦截器和刷新token拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(
                        "/user/code",
                        "/user/login",
                        "/messageTemplate/receipt",
                        "/officialAccount/receipt"
                ).order(1);
        // 刷新token拦截器
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate)).addPathPatterns("/**").order(0);
    }

    /**
     * 跨域配置
     * @param registry
     */
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//                // 设置允许跨域的路径
//        registry.addMapping("/**")
//                // 设置允许跨域请求的域名
//                .allowedOriginPatterns("*")
//                // 是否允许证书
//                .allowCredentials(true)
//                // 设置允许的请求方式
//                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
//                // 设置允许的header属性
//                .allowedHeaders("*")
//                // 设置跨域允许时间
//                .maxAge(3600);
//    }
}
