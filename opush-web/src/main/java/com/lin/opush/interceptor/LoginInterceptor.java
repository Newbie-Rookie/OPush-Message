package com.lin.opush.interceptor;

import com.lin.opush.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用户登录拦截器
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 区分前端资源请求
        String sign = request.getHeader("sign");
        // 1.判断是否需要拦截（ThreadLocal中是否有用户）
        if (UserHolder.getUser() == null) {
            // 没有用户（动态资源请求重定向给login.html，静态资源请求标记为已登录再放行）
            if(sign == null){
                // 动态资源则拦截并重定向到login.html
                response.setStatus(401);
                response.sendRedirect("http://localhost:3000");
                // throw new CommonException(RespStatusEnum.NO_LOGIN.getCode(), RespStatusEnum.NO_LOGIN.getMsg());
                // 拦截
                return false;
            } else {
                // 标记为未登录再放行
                request.setAttribute("status", "0");
                return true;
            }
        }
        // 有用户（动态资源请求直接放行，静态资源请求标记为已登录再放行）
        if(sign != null){
            // 标记为已登录再放行
            request.setAttribute("status", "1");
        }
        return true;
    }
}
