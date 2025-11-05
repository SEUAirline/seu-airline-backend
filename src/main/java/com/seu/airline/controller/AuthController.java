package com.seu.airline.controller;

import com.seu.airline.dto.ApiResponse;
import com.seu.airline.dto.LoginDTO;
import com.seu.airline.dto.UserDTO;
import com.seu.airline.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            var response = authService.login(loginDTO);
            return ResponseEntity.ok(ApiResponse.success(response, "登录成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户名或密码错误"));
        }
    }

    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDTO userDTO) {
        try {
            var response = authService.register(userDTO);
            return ResponseEntity.ok(ApiResponse.success(response, "注册成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 获取当前用户信息
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            var userDetails = authService.getCurrentUser(authentication);
            return ResponseEntity.ok(ApiResponse.success(userDetails, "获取用户信息成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取用户信息失败"));
        }
    }

    // 用户登出
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        if (authService.logout(request)) {
            return ResponseEntity.ok(ApiResponse.success("登出成功"));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("无效的token"));
    }

    // 刷新token
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        try {
            var response = authService.refreshToken(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Token刷新成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}