package com.seu.airline.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties("orderItems")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    @JsonIgnoreProperties("orderItems")
    private Seat seat;

    @Column(name = "passenger_name", nullable = false, length = 100)
    private String passengerName;

    @Column(name = "passenger_id_card", length = 50)
    private String passengerIdCard;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}