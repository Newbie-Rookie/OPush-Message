package com.lin.opush.service;

import com.lin.opush.dto.UserDTO;
import com.lin.opush.vo.BasicResultVO;

/**
 * 用户服务接口
 */
public interface UserService {

    BasicResultVO sendCode(String phone);

    BasicResultVO login(UserDTO userDTO);

    BasicResultVO logout(String token);
}
