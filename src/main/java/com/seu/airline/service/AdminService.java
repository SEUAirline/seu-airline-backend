package com.seu.airline.service;

import com.seu.airline.model.Order;
import com.seu.airline.model.User;
import java.util.List;

public interface AdminService {
    
    // 获取所有用户
    List<User> getAllUsers();
    
    // 根据角色获取用户
    List<User> getUsersByRole(String role);
    
    // 更新用户状态
    User updateUserStatus(Long id, Integer status);
    
    // 获取所有订单
    List<Order> getAllOrders();
    
    // 根据状态获取订单
    List<Order> getOrdersByStatus(String status);
    
    // 根据时间段获取订单
    List<Order> getOrdersByPeriod(String startDate, String endDate);
    
    // 获取指定用户的订单
    List<Order> getOrdersByUser(Long userId);
}