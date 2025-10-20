package com.seu.airline.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "seats", uniqueConstraints = @UniqueConstraint(columnNames = {"flight_id", "seat_number"}))
@Data
@NoArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    @JsonIgnoreProperties("seats")
    private Flight flight;

    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false)
    private SeatType seatType;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 座位类型枚举
    public enum SeatType {
        ECONOMY, BUSINESS, FIRST
    }

    // 座位状态枚举
    public enum SeatStatus {
        AVAILABLE, RESERVED, OCCUPIED
    }
}