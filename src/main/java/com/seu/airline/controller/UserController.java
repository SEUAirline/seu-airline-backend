package com.seu.airline.controller;

import com.seu.airline.dto.ApiResponse;
import com.seu.airline.dto.UserProfileDTO;
import com.seu.airline.model.User;
import com.seu.airline.repository.UserRepository;
import com.seu.airline.security.UserDetailsImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息管理控制器
 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户信息管理")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取当前登录用户的详细信息
     */
    @GetMapping("/profile")
    @ApiOperation("获取当前用户信息")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("未认证用户尝试访问 /user/profile");
                return ResponseEntity
                        .status(401)
                        .body(ApiResponse.error("未登录或登录已过期"));
            }

            // 从 Authentication 对象中获取用户信息
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            log.info("用户 {} 请求个人信息", username);

            // 从数据库获取完整的用户信息
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 使用 DTO 避免 Hibernate 懒加载序列化问题
            UserProfileDTO profileDTO = UserProfileDTO.fromUser(user);
            
            return ResponseEntity.ok(ApiResponse.success(profileDTO, "获取用户信息成功"));
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("获取用户信息失败: " + e.getMessage()));
        }
    }

    /**
     * 更新当前用户的个人资料
     */
    @PutMapping("/profile")
    @ApiOperation("更新当前用户信息")
    public ResponseEntity<?> updateUserProfile(
            Authentication authentication,
            @RequestBody UserProfileDTO profileDTO) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity
                        .status(401)
                        .body(ApiResponse.error("未登录或登录已过期"));
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            log.info("用户 {} 更新个人信息", username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 更新允许修改的字段
            if (profileDTO.getFullName() != null) {
                user.setFullName(profileDTO.getFullName());
            }
            if (profileDTO.getEmail() != null) {
                user.setEmail(profileDTO.getEmail());
            }
            if (profileDTO.getPhone() != null) {
                user.setPhone(profileDTO.getPhone());
            }
            if (profileDTO.getIdCard() != null) {
                user.setIdCard(profileDTO.getIdCard());
            }

            userRepository.save(user);
            
            return ResponseEntity.ok(ApiResponse.success(null, "更新成功"));
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("更新用户信息失败: " + e.getMessage()));
        }
    }

    /**
     * 获取当前用户ID（辅助方法）
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getId();
        }
        throw new RuntimeException("无法获取当前用户信息");
    }
}
