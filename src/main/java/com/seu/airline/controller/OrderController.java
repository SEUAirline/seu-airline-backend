package com.seu.airline.controller;

import com.seu.airline.config.RabbitMQConfig;
import com.seu.airline.dto.OrderDTO;
import com.seu.airline.model.rabbitmq.OrderMessage;
import com.seu.airline.service.OrderService;
import com.seu.airline.service.rabbitmq.RabbitMQSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private RabbitMQSenderService rabbitMQSenderService;

    // 订单请求DTO
    public static class OrderRequest {
        private List<OrderItemRequest> items;
        private String flightNumber;

        // getter and setter
        public List<OrderItemRequest> getItems() {
            return items;
        }

        public void setItems(List<OrderItemRequest> items) {
            this.items = items;
        }

        public String getFlightNumber() {
            return flightNumber;
        }

        public void setFlightNumber(String flightNumber) {
            this.flightNumber = flightNumber;
        }
    }

    // 订单项请求DTO
    public static class OrderItemRequest {
        private Long seatId;
        private String passengerName;
        private String passengerIdCard;

        // getter and setter
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

    /**
     * 创建订单 - 异步处理
     */
    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest orderRequest) {
        Long userId = getCurrentUserId();
        
        // 创建订单消息
        OrderMessage orderMessage = new OrderMessage();
        orderMessage.setMessageId(UUID.randomUUID().toString());
        orderMessage.setSource("API");
        orderMessage.setTimestamp(LocalDateTime.now());
        orderMessage.setType("ORDER_CREATE");
        orderMessage.setUserId(userId.toString());
        orderMessage.setFlightNumber(orderRequest.getFlightNumber());
        
        // 将订单项详情添加到消息中
        orderMessage.setItems(orderRequest.getItems().stream()
                .map(item -> new OrderMessage.OrderItemDetail(
                        item.getSeatId(),
                        item.getPassengerName(),
                        item.getPassengerIdCard()))
                .toList());
        
        // 发送到订单队列
        rabbitMQSenderService.sendMessage(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_ROUTING_KEY,
                orderMessage
        );
        
        return ResponseEntity.ok("订单请求已接收，正在处理中");
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getUserOrders() {
        Long userId = getCurrentUserId();
        List<OrderDTO> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();
        OrderDTO order = orderService.getOrderById(orderId, userId);
        return ResponseEntity.ok(order);
    }

    /**
     * 取消订单
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();
        OrderDTO order = orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(order);
    }

    /**
     * 支付订单
     */
    @PutMapping("/{orderId}/pay")
    public ResponseEntity<OrderDTO> payOrder(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();
        OrderDTO order = orderService.payOrder(orderId, userId);
        return ResponseEntity.ok(order);
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            // 这里假设用户ID存储在username字段中，实际应用中可能需要调整
            return Long.parseLong(userDetails.getUsername());
        }
        throw new RuntimeException("用户未登录");
    }
}