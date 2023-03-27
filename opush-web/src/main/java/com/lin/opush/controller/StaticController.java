package com.lin.opush.controller;

import com.lin.opush.vo.BasicResultVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class StaticController {
    @GetMapping("/static")
    public BasicResultVO loginStatic(HttpServletRequest request){
        String status = (String) request.getAttribute("status");
        // 判断是否已登录
        if("0".equals(status)){
            // 未登录
            return BasicResultVO.fail();
        }
        // 已登录
        return BasicResultVO.success();
    }
}
