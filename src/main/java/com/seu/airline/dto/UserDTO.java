package com.seu.airline.dto;

import com.seu.airline.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;

    @NotBlank(message = "用户名不能为空", groups = { ValidationGroups.Create.class })
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空", groups = { ValidationGroups.Create.class })
    @Size(min = 6, message = "密码长度至少为6个字符")
    private String password;

    @NotBlank(message = "邮箱不能为空", groups = { ValidationGroups.Create.class })
    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;
    private String fullName;
    private String name; // 前端使用name字段
    private String idCard;
    private String role;
    private Integer vipLevel;
    private Integer points;
    private String createdAt;

    // 从User实体构造UserDTO
    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.fullName = user.getFullName();
        this.name = user.getFullName(); // name字段作为fullName的别名
        this.idCard = null; // User模型中暂无idCard字段
        this.role = user.getRole() != null ? user.getRole().name().toLowerCase() : "user";
        // 前端需要这些字段，先设置默认值
        this.vipLevel = 0;
        this.points = 0;
        this.createdAt = user.getCreatedAt() != null ? user.getCreatedAt().toString() : null;
    }

    // 验证组接口
    public interface ValidationGroups {
        interface Create {
        }
    }
}