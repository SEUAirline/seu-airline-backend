package com.seu.airline.dto;

import com.seu.airline.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private UserDTO user;
    private String token;

    public AuthResponseDTO(User user, String token) {
        this.user = new UserDTO(user);
        this.token = token;
    }
}
