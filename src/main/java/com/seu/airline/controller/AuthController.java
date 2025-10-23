package com.seu.airline.controller;

import com.seu.airline.dto.ApiResponse;
import com.seu.airline.dto.AuthResponseDTO;
import com.seu.airline.dto.LoginDTO;
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

@RestController
@RequestMapping("/api/auth")
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
        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 获取完整的用户信息
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("用户不存在"));
            }

            // 返回AuthResponseDTO格式：{user, token}
            AuthResponseDTO response = new AuthResponseDTO(user, jwt);
            return ResponseEntity.ok(ApiResponse.success(response, "登录成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户名或密码错误"));
        }
    } // 用户注册

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

        User savedUser = userRepository.save(user);

        // 生成token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDTO.getUsername(), userDTO.getPassword()));
        String jwt = jwtUtils.generateJwtToken(authentication);

        // 返回AuthResponseDTO格式：{user, token}
        AuthResponseDTO response = new AuthResponseDTO(savedUser, jwt);
        return ResponseEntity.ok(response);
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
            return ResponseEntity.ok(ApiResponse.success("登出成功"));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("无效的token"));
    }

    // 刷新token
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String oldToken = parseJwt(request);
        if (oldToken == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("无效的token"));
        }

        // 验证旧token
        if (!jwtUtils.validateJwtToken(oldToken)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("token已失效或无效"));
        }

        // 生成新token
        String newToken = jwtUtils.refreshToken(oldToken);
        if (newToken == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("刷新token失败"));
        }

        // 获取用户信息
        String username = jwtUtils.getUserNameFromJwtToken(newToken);
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户不存在"));
        }

        AuthResponseDTO response = new AuthResponseDTO(user, newToken);
        return ResponseEntity.ok(ApiResponse.success(response, "Token刷新成功"));
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