package com.seu.airline.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 乘客信息创建请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassengerCreateRequest {

    /**
     * 乘客姓名
     */
    @NotBlank(message = "乘客姓名不能为空")
    @Size(min = 2, max = 100, message = "乘客姓名长度必须在2-100个字符之间")
    private String passengerName;

    /**
     * 证件类型（ID_CARD-身份证/PASSPORT-护照/OTHER-其他）
     */
    @NotBlank(message = "证件类型不能为空")
    @Pattern(regexp = "^(ID_CARD|PASSPORT|OTHER)$", message = "证件类型必须是ID_CARD、PASSPORT或OTHER")
    private String idType;

    /**
     * 证件号码
     */
    @NotBlank(message = "证件号码不能为空")
    @Size(max = 50, message = "证件号码长度不能超过50个字符")
    private String idCard;

    /**
     * 联系电话
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱地址
     */
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "邮箱格式不正确")
    private String email;

    /**
     * 乘客类型（ADULT-成人/CHILD-儿童/INFANT-婴儿）
     */
    @NotBlank(message = "乘客类型不能为空")
    @Pattern(regexp = "^(ADULT|CHILD|INFANT)$", message = "乘客类型必须是ADULT、CHILD或INFANT")
    private String passengerType;

    /**
     * 是否设为默认乘客
     */
    private Boolean isDefault = false;
}
