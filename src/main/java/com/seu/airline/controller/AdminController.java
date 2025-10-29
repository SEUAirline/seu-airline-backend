package com.seu.airline.controller;

import com.seu.airline.dto.ApiResponse;
import com.seu.airline.model.Order;
import com.seu.airline.model.User;
import com.seu.airline.repository.OrderRepository;
import com.seu.airline.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    // 获取所有用户
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(users, "获取用户列表成功"));
    }

    // 根据角色获取用户
    @GetMapping("/users/role/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        try {
            User.Role userRole = User.Role.valueOf(role.toUpperCase());
            List<User> users = userRepository.findByRole(userRole);
            return ResponseEntity.ok(ApiResponse.success(users, "获取" + role + "角色用户成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("无效的角色类型: " + role));
        }
    }

    // 更新用户状态
    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(404).body(ApiResponse.error("用户不存在"));
        }

        User user = userOpt.get();
        // 不允许禁用管理员账户
        if (user.getRole() == User.Role.ADMIN && status == 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("不能禁用管理员账户"));
        }

        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success(user, "用户状态更新成功"));
    }

    // 获取所有订单
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(orders, "获取订单列表成功"));
    }

    // 根据状态获取订单
    @GetMapping("/orders/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            List<Order> orders = orderRepository.findByStatus(orderStatus);
            return ResponseEntity.ok(ApiResponse.success(orders, "获取" + status + "状态订单成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("无效的订单状态: " + status));
        }
    }

    // 获取指定时间段内的订单
    @GetMapping("/orders/period")
    public ResponseEntity<?> getOrdersByPeriod(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");

            List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);
            return ResponseEntity.ok(ApiResponse.success(orders, "获取时间段内订单成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("日期格式错误，请使用YYYY-MM-DD格式"));
        }
    }

    // 获取指定用户的订单
    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<?> getOrdersByUser(@PathVariable Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(orders, "获取用户订单成功"));
    }
}