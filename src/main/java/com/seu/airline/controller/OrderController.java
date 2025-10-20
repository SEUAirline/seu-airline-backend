package com.seu.airline.controller;

import com.seu.airline.model.Order;
import com.seu.airline.model.OrderItem;
import com.seu.airline.model.Seat;
import com.seu.airline.model.User;
import com.seu.airline.repository.OrderItemRepository;
import com.seu.airline.repository.OrderRepository;
import com.seu.airline.repository.SeatRepository;
import com.seu.airline.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private SeatRepository seatRepository;

    // 创建订单
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestBody OrderRequest orderRequest,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        try {
            // 创建订单
            Order order = new Order();
            order.setOrderNumber(generateOrderNumber());
            User user = new User();
        user.setId(userDetails.getId());
        order.setUser(user);
            order.setStatus(Order.OrderStatus.PENDING);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            
            // 计算总金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OrderItemRequest item : orderRequest.getItems()) {
                Optional<Seat> seatOpt = seatRepository.findById(item.getSeatId());
                if (!seatOpt.isPresent()) {
                    return ResponseEntity.badRequest().body("座位不存在");
                }
                
                Seat seat = seatOpt.get();
                if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
                    return ResponseEntity.badRequest().body("座位已被占用");
                }
                
                totalAmount = totalAmount.add(seat.getPrice());
            }
            
            order.setTotalAmount(totalAmount);
            Order savedOrder = orderRepository.save(order);
            
            // 创建订单详情
            for (OrderItemRequest item : orderRequest.getItems()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(savedOrder);
                
                Optional<Seat> seatOpt = seatRepository.findById(item.getSeatId());
                if (seatOpt.isPresent()) {
                    Seat seat = seatOpt.get();
                    orderItem.setSeat(seat);
                    orderItem.setPassengerName(item.getPassengerName());
                    orderItem.setPassengerIdCard(item.getPassengerIdCard());
                    orderItem.setPrice(seat.getPrice());
                    orderItem.setCreatedAt(LocalDateTime.now());
                    
                    // 更新座位状态
                    seat.setStatus(Seat.SeatStatus.RESERVED);
                    seatRepository.save(seat);
                    
                    orderItemRepository.save(orderItem);
                }
            }
            
            return ResponseEntity.ok(savedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("创建订单失败: " + e.getMessage());
        }
    }

    // 获取用户的所有订单
    @GetMapping
    public ResponseEntity<?> getUserOrders(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<Order> orders = orderRepository.findByUserId(userDetails.getId());
        return ResponseEntity.ok(orders);
    }

    // 获取订单详情
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(
            @PathVariable Long id,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (!orderOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Order order = orderOpt.get();
        // 检查是否是用户自己的订单
        if (!order.getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body("无权访问此订单");
        }
        
        // 获取订单详情
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(id);
        OrderResponse response = new OrderResponse(order, orderItems);
        
        return ResponseEntity.ok(response);
    }

    // 取消订单
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long id,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (!orderOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Order order = orderOpt.get();
        if (!order.getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body("无权操作此订单");
        }
        
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            return ResponseEntity.badRequest().body("只能取消待支付的订单");
        }
        
        // 更新订单状态
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        
        // 释放座位
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(id);
        for (OrderItem item : orderItems) {
            Optional<Seat> seatOpt = seatRepository.findById(item.getSeat().getId());
            if (seatOpt.isPresent()) {
                Seat seat = seatOpt.get();
                seat.setStatus(Seat.SeatStatus.AVAILABLE);
                seatRepository.save(seat);
            }
        }
        
        return ResponseEntity.ok(order);
    }

    // 支付订单
    @PutMapping("/{id}/pay")
    public ResponseEntity<?> payOrder(
            @PathVariable Long id,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (!orderOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Order order = orderOpt.get();
        if (!order.getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).body("无权操作此订单");
        }
        
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            return ResponseEntity.badRequest().body("订单已处理");
        }
        
        // 更新订单状态
        order.setStatus(Order.OrderStatus.PAID);
        order.setPaymentTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        
        // 更新座位状态为已占用
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(id);
        for (OrderItem item : orderItems) {
            Optional<Seat> seatOpt = seatRepository.findById(item.getSeat().getId());
            if (seatOpt.isPresent()) {
                Seat seat = seatOpt.get();
                seat.setStatus(Seat.SeatStatus.OCCUPIED);
                seatRepository.save(seat);
            }
        }
        
        return ResponseEntity.ok(order);
    }

    // 生成订单号
    private String generateOrderNumber() {
        return "ORD" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    // 请求和响应类
    public static class OrderItemRequest {
        private Long seatId;
        private String passengerName;
        private String passengerIdCard;
        
        // getters and setters
        public Long getSeatId() { return seatId; }
        public void setSeatId(Long seatId) { this.seatId = seatId; }
        public String getPassengerName() { return passengerName; }
        public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
        public String getPassengerIdCard() { return passengerIdCard; }
        public void setPassengerIdCard(String passengerIdCard) { this.passengerIdCard = passengerIdCard; }
    }

    public static class OrderRequest {
        private List<OrderItemRequest> items;
        
        // getters and setters
        public List<OrderItemRequest> getItems() { return items; }
        public void setItems(List<OrderItemRequest> items) { this.items = items; }
    }

    public static class OrderResponse {
        private Order order;
        private List<OrderItem> items;
        
        public OrderResponse(Order order, List<OrderItem> items) {
            this.order = order;
            this.items = items;
        }
        
        // getters and setters
        public Order getOrder() { return order; }
        public void setOrder(Order order) { this.order = order; }
        public List<OrderItem> getItems() { return items; }
        public void setItems(List<OrderItem> items) { this.items = items; }
    }
}