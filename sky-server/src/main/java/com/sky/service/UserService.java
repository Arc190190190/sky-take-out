package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

/**
 * @author Arc
 * @version v1.0
 */
public interface UserService {
    /**
     * 微信登录
     * @return
     */
    User wxlogin(UserLoginDTO userLoginDTO);
}
