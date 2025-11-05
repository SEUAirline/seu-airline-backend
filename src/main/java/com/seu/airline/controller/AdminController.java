package com.seu.airline.controller;

import com.seu.airline.dto.ApiResponse;
import com.seu.airline.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    @Autowired
    private AdminService adminService;

    // 获取所有用户
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<?> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "获取用户列表成功"));
    }

    // 根据角色获取用户
    @GetMapping("/users/role/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        try {
            List<?> users = adminService.getUsersByRole(role);
            return ResponseEntity.ok(ApiResponse.success(users, "获取" + role + "角色用户成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("无效的角色类型: " + role));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 更新用户状态
    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        try {
            Object user = adminService.updateUserStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success(user, "用户状态更新成功"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 获取所有订单
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders() {
        List<?> orders = adminService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(orders, "获取订单列表成功"));
    }

    // 根据状态获取订单
    @GetMapping("/orders/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            List<?> orders = adminService.getOrdersByStatus(status);
            return ResponseEntity.ok(ApiResponse.success(orders, "获取" + status + "状态订单成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("无效的订单状态: " + status));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 获取指定时间段内的订单
    @GetMapping("/orders/period")
    public ResponseEntity<?> getOrdersByPeriod(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            List<?> orders = adminService.getOrdersByPeriod(startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(orders, "获取时间段内订单成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("日期格式错误，请使用YYYY-MM-DD格式"));
        }
    }

    // 获取指定用户的订单
    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<?> getOrdersByUser(@PathVariable Long userId) {
        try {
            List<?> orders = adminService.getOrdersByUser(userId);
            return ResponseEntity.ok(ApiResponse.success(orders, "获取用户订单成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}