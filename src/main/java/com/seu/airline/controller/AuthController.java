package com.seu.airline.controller;

import com.seu.airline.dto.LoginDTO;
import com.seu.airline.dto.TokenDTO;
import com.seu.airline.dto.UserDTO;
import com.seu.airline.model.User;
import com.seu.airline.repository.UserRepository;
import com.seu.airline.security.JwtUtils;
import com.seu.airline.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginDTO loginDTO) {
        // 认证用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 返回token和用户信息
        return ResponseEntity.ok(new TokenDTO(
                jwt,
                "Bearer",
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getRole()));
    }

    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDTO userDTO) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("错误：用户名已被使用！");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("错误：邮箱已被注册！");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(encoder.encode(userDTO.getPassword())); // 加密密码
        user.setPhone(userDTO.getPhone());
        user.setFullName(userDTO.getFullName());
        user.setRole(User.Role.PASSENGER); // 默认普通用户
        user.setStatus(1); // 默认启用状态

        userRepository.save(user);

        return ResponseEntity.ok("用户注册成功！");
    }

    // 获取当前用户信息
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(userDetails);
    }

    // 用户登出
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        String jwt = parseJwt(request);
        if (jwt != null) {
            // 将token加入黑名单
            jwtUtils.addTokenToBlacklist(jwt);
            return ResponseEntity.ok("登出成功！");
        }
        return ResponseEntity.badRequest().body("无效的token");
    }

    // 刷新token
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String oldToken = parseJwt(request);
        if (oldToken == null) {
            return ResponseEntity.badRequest().body("无效的token");
        }

        // 验证旧token
        if (!jwtUtils.validateJwtToken(oldToken)) {
            return ResponseEntity.badRequest().body("token已失效或无效");
        }

        // 生成新token
        String newToken = jwtUtils.refreshToken(oldToken);
        if (newToken == null) {
            return ResponseEntity.badRequest().body("刷新token失败");
        }

        // 获取用户信息
        String username = jwtUtils.getUserNameFromJwtToken(newToken);
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("用户不存在");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("token", newToken);
        response.put("type", "Bearer");
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("role", user.getRole());

        return ResponseEntity.ok(response);
    }

    // 从请求头中解析JWT token
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}