package com.seu.airline.service;

import com.seu.airline.dto.OrderDTO;
import com.seu.airline.controller.OrderController;

import java.util.List;

public interface OrderService {
    
    /**
     * 创建订单
     */
    OrderDTO createOrder(OrderController.OrderRequest orderRequest, Long userId);
    
    /**
     * 获取用户的所有订单
     */
    List<OrderDTO> getUserOrders(Long userId);
    
    /**
     * 获取订单详情
     */
    OrderDTO getOrderById(Long orderId, Long userId);
    
    /**
     * 取消订单
     */
    OrderDTO cancelOrder(Long orderId, Long userId);
    
    /**
     * 支付订单
     */
    OrderDTO payOrder(Long orderId, Long userId);
    
    /**
     * 生成订单号
     */
    String generateOrderNumber();
}