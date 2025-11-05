package com.seu.airline.controller;

import com.seu.airline.dto.ApiResponse;
import com.seu.airline.dto.OrderDTO;
import com.seu.airline.security.UserDetailsImpl;
import com.seu.airline.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderController {

    @Autowired
    private OrderService orderService;

    // 创建订单
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestBody OrderRequest orderRequest,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        try {
            OrderDTO orderDTO = orderService.createOrder(orderRequest, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success(orderDTO, "订单创建成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("创建订单失败: " + e.getMessage()));
        }
    }

    // 获取用户的所有订单
    @GetMapping
    public ResponseEntity<?> getUserOrders(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<OrderDTO> orderDTOs = orderService.getUserOrders(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(orderDTOs));
    } // 获取订单详情

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(
            @PathVariable Long id,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        try {
            OrderDTO orderDTO = orderService.getOrderById(id, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success(orderDTO));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("订单不存在")) {
                return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
            }
            if (e.getMessage().equals("无权访问此订单")) {
                return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    } // 取消订单

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long id,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        try {
            OrderDTO orderDTO = orderService.cancelOrder(id, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success(orderDTO, "订单已取消"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("订单不存在")) {
                return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
            }
            if (e.getMessage().equals("无权操作此订单")) {
                return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 支付订单
    @PutMapping("/{id}/pay")
    public ResponseEntity<?> payOrder(
            @PathVariable Long id,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        try {
            OrderDTO orderDTO = orderService.payOrder(id, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success(orderDTO, "支付成功"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("订单不存在")) {
                return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
            }
            if (e.getMessage().equals("无权操作此订单")) {
                return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 请求和响应类
    public static class OrderItemRequest {
        private Long seatId;
        private String passengerName;
        private String passengerIdCard;

        // getters and setters
        public Long getSeatId() {
            return seatId;
        }

        public void setSeatId(Long seatId) {
            this.seatId = seatId;
        }

        public String getPassengerName() {
            return passengerName;
        }

        public void setPassengerName(String passengerName) {
            this.passengerName = passengerName;
        }

        public String getPassengerIdCard() {
            return passengerIdCard;
        }

        public void setPassengerIdCard(String passengerIdCard) {
            this.passengerIdCard = passengerIdCard;
        }
    }

    public static class OrderRequest {
        private List<OrderItemRequest> items;

        // getters and setters
        public List<OrderItemRequest> getItems() {
            return items;
        }

        public void setItems(List<OrderItemRequest> items) {
            this.items = items;
        }
    }


}