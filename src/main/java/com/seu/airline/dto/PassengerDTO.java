package com.seu.airline.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 乘客信息DTO - 返回数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassengerDTO {
    
    private Long id;
    
    /**
     * 乘客姓名
     */
    private String passengerName;
    
    /**
     * 证件类型
     */
    private String idType;
    
    /**
     * 证件号码
     */
    private String idCard;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 邮箱地址
     */
    private String email;
    
    /**
     * 乘客类型
     */
    private String passengerType;
    
    /**
     * 是否默认乘客
     */
    private Boolean isDefault;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
