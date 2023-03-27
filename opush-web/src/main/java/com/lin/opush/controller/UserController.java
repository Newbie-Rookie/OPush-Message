package com.lin.opush.controller;

import com.lin.opush.dto.UserDTO;
import com.lin.opush.service.UserService;
import com.lin.opush.vo.BasicResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户登录、注册、登出控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送手机验证码
     * @param phone 手机号
     * @return 发送结果
     */
    @PostMapping("/code")
    public BasicResultVO sendCode(String phone){
        return userService.sendCode(phone);
    }

    /**
     * 用户登录
     * @param userDTO 用户登录信息（包含手机号和验证码）
     * @return
     */
    @PostMapping("/login")
    public BasicResultVO login(UserDTO userDTO){
        return userService.login(userDTO);
    }

    /**
     * 用户登出
     * @return 无
     */
    @PostMapping("/logout")
    public BasicResultVO logout(String token){
        return userService.logout(token);
    }
}
