package com.seu.airline.dto;

import com.seu.airline.model.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户个人资料 DTO
 * 用于返回用户基本信息,避免 Hibernate 懒加载序列化问题
 */
@Data
public class UserProfileDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 可选的额外字段
    private String nickname;
    private String avatar;
    private String gender;
    private String birthday;
    private String idCard;

    /**
     * 从 User 实体创建 DTO
     */
    public static UserProfileDTO fromUser(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setIdCard(user.getIdCard());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        // 这些字段目前 User 实体中没有,可以后续扩展
        // dto.setNickname(user.getNickname());
        // dto.setAvatar(user.getAvatar());
        // dto.setGender(user.getGender());
        // dto.setBirthday(user.getBirthday());
        
        return dto;
    }
}
