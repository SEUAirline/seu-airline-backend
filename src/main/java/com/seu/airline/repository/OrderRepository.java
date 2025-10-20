package com.seu.airline.repository;

import com.seu.airline.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 根据订单号查找
    Optional<Order> findByOrderNumber(String orderNumber);

    // 根据用户ID查找订单
    List<Order> findByUserId(Long userId);

    // 根据用户ID和订单状态查找
    List<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status);

    // 查询特定状态的订单
    List<Order> findByStatus(Order.OrderStatus status);

    // 查询指定时间段内的订单
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}