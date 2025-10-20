package com.seu.airline.dto;

import com.seu.airline.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenDTO {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private String role;
    
    // 重载构造函数，接受User.Role枚举
    public TokenDTO(String token, String type, Long userId, String username, User.Role role) {
        this.token = token;
        this.type = type;
        this.userId = userId;
        this.username = username;
        this.role = role != null ? role.name() : null;
    }
}