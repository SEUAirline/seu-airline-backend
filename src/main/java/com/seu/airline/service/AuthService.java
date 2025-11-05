package com.seu.airline.service;

import com.seu.airline.dto.AuthResponseDTO;
import com.seu.airline.dto.LoginDTO;
import com.seu.airline.dto.UserDTO;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

public interface AuthService {
    
    /**
     * 用户登录认证
     * @param loginDTO 登录信息
     * @return 认证响应（用户信息和token）
     */
    AuthResponseDTO login(LoginDTO loginDTO);
    
    /**
     * 用户注册
     * @param userDTO 用户注册信息
     * @return 注册后的用户和token
     */
    AuthResponseDTO register(UserDTO userDTO);
    
    /**
     * 获取当前用户信息
     * @param authentication 认证对象
     * @return 当前登录用户
     */
    Object getCurrentUser(Authentication authentication);
    
    /**
     * 用户登出
     * @param request 请求对象
     * @return 登出结果
     */
    boolean logout(HttpServletRequest request);
    
    /**
     * 刷新Token
     * @param request 请求对象
     * @return 新的认证响应
     */
    AuthResponseDTO refreshToken(HttpServletRequest request);
    
    /**
     * 从请求头中解析JWT token
     * @param request 请求对象
     * @return token字符串
     */
    String parseJwt(HttpServletRequest request);
}