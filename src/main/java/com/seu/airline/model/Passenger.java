package com.seu.airline.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 乘客信息实体类
 */
@Entity
@Table(name = "passengers")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 乘客姓名
     */
    @Column(name = "passenger_name", nullable = false, length = 100)
    private String passengerName;

    /**
     * 证件类型（ID_CARD-身份证/PASSPORT-护照/OTHER-其他）
     */
    @Column(name = "id_type", length = 20)
    private String idType = "ID_CARD";

    /**
     * 证件号码
     */
    @Column(name = "id_card", nullable = false, length = 50)
    private String idCard;

    /**
     * 联系电话
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 邮箱地址
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 乘客类型（ADULT-成人/CHILD-儿童/INFANT-婴儿）
     */
    @Column(name = "passenger_type", nullable = false, length = 20)
    private String passengerType = "ADULT";

    /**
     * 是否默认乘客（true-是，false-否）
     */
    @Column(name = "is_default")
    private Boolean isDefault = false;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
