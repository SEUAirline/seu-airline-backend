package com.seu.airline.service.impl;

import com.seu.airline.dto.AuthResponseDTO;
import com.seu.airline.dto.LoginDTO;
import com.seu.airline.dto.UserDTO;
import com.seu.airline.model.User;
import com.seu.airline.repository.UserRepository;
import com.seu.airline.security.JwtUtils;
import com.seu.airline.security.UserDetailsImpl;
import com.seu.airline.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public AuthResponseDTO login(LoginDTO loginDTO) {
        // 认证用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 获取完整的用户信息
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 返回AuthResponseDTO格式：{user, token}
        return new AuthResponseDTO(user, jwt);
    }

    @Override
    @Transactional
    public AuthResponseDTO register(UserDTO userDTO) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("用户名已被使用");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
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
        return new AuthResponseDTO(savedUser, jwt);
    }

    @Override
    public Object getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails;
    }

    @Override
    public boolean logout(HttpServletRequest request) {
        String jwt = parseJwt(request);
        if (jwt != null) {
            // 将token加入黑名单
            jwtUtils.addTokenToBlacklist(jwt);
            return true;
        }
        return false;
    }

    @Override
    public AuthResponseDTO refreshToken(HttpServletRequest request) {
        String oldToken = parseJwt(request);
        if (oldToken == null) {
            throw new RuntimeException("无效的token");
        }

        // 验证旧token
        if (!jwtUtils.validateJwtToken(oldToken)) {
            throw new RuntimeException("token已失效或无效");
        }

        // 生成新token
        String newToken = jwtUtils.refreshToken(oldToken);
        if (newToken == null) {
            throw new RuntimeException("刷新token失败");
        }

        // 获取用户信息
        String username = jwtUtils.getUserNameFromJwtToken(newToken);
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        return new AuthResponseDTO(user, newToken);
    }

    @Override
    public String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}