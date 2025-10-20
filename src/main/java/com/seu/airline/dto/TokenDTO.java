package com.seu.airline.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenDTO {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private Integer role;
}