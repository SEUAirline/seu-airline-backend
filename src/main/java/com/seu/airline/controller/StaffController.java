package com.seu.airline.controller;

import com.seu.airline.dto.ApiResponse;
import com.seu.airline.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/staff")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StaffController {

    @Autowired
    private StaffService staffService;

    // 获取所有航班
    @GetMapping("/flights")
    public ResponseEntity<?> getAllFlights() {
        List<?> flights = staffService.getAllFlights();
        return ResponseEntity.ok(ApiResponse.success(flights, "获取航班列表成功"));
    }

    // 获取特定状态的航班
    @GetMapping("/flights/status/{status}")
    public ResponseEntity<?> getFlightsByStatus(@PathVariable String status) {
        try {
            List<?> flights = staffService.getFlightsByStatus(status);
            return ResponseEntity.ok(ApiResponse.success(flights, "获取" + status + "状态航班成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("无效的航班状态: " + status));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 更新航班状态
    @PutMapping("/flights/{id}/status")
    public ResponseEntity<?> updateFlightStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            Object flight = staffService.updateFlightStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success(flight, "航班状态更新成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("无效的航班状态: " + status));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 获取航班乘客信息
    @GetMapping("/flights/{id}/passengers")
    public ResponseEntity<?> getFlightPassengers(@PathVariable Long id) {
        try {
            List<?> passengers = staffService.getFlightPassengers(id);
            return ResponseEntity.ok(ApiResponse.success(passengers, "获取航班乘客信息成功"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 查看订单详情（用于核对乘客信息）
    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getOrderDetails(@PathVariable Long id) {
        try {
            Object response = staffService.getOrderDetails(id);
            return ResponseEntity.ok(ApiResponse.success(response, "获取订单详情成功"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 手动取消超时未支付的订单
    @PutMapping("/orders/{id}/cancel-overdue")
    public ResponseEntity<?> cancelOverdueOrder(@PathVariable Long id) {
        try {
            Object order = staffService.cancelOverdueOrder(id);
            return ResponseEntity.ok(ApiResponse.success(order, "超时订单已取消"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}