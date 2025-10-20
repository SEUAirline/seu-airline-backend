package com.seu.airline.repository;

import com.seu.airline.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 根据订单ID查找订单详情
    List<OrderItem> findByOrderId(Long orderId);

    // 根据座位ID查找订单详情
    List<OrderItem> findBySeatId(Long seatId);
}