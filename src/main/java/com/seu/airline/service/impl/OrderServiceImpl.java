package com.seu.airline.service.impl;

import com.seu.airline.controller.OrderController;
import com.seu.airline.dto.OrderDTO;
import com.seu.airline.model.Order;
import com.seu.airline.model.OrderItem;
import com.seu.airline.model.Seat;
import com.seu.airline.model.User;
import com.seu.airline.repository.OrderItemRepository;
import com.seu.airline.repository.OrderRepository;
import com.seu.airline.repository.SeatRepository;
import com.seu.airline.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private RedisLockService redisLockService;

    @Override
    @Transactional
    public OrderDTO createOrder(OrderController.OrderRequest orderRequest, Long userId) {
        // 创建订单对象
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        User user = new User();
        user.setId(userId);
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // 先验证所有座位是否存在并可预订
        for (OrderController.OrderItemRequest item : orderRequest.getItems()) {
            Optional<Seat> seatOpt = seatRepository.findById(item.getSeatId());
            if (!seatOpt.isPresent()) {
                throw new RuntimeException("座位不存在: " + item.getSeatId());
            }
            
            Seat seat = seatOpt.get();
            if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
                throw new RuntimeException("座位已被占用: " + item.getSeatId());
            }
            
            totalAmount = totalAmount.add(seat.getPrice());
        }
        
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        
        log.info("创建订单成功: orderNumber={}, userId={}, seatCount={}", 
                 order.getOrderNumber(), userId, orderRequest.getItems().size());

        // 对每个座位使用分布式锁进行保护，确保并发安全
        for (OrderController.OrderItemRequest item : orderRequest.getItems()) {
            try {
                // 使用分布式锁保护座位资源，超时时间5秒
                redisLockService.executeWithLock(item.getSeatId(), 5000, () -> {
                    // 再次检查座位状态（双重检查锁模式）
                    Optional<Seat> seatOpt = seatRepository.findById(item.getSeatId());
                    if (!seatOpt.isPresent()) {
                        throw new RuntimeException("座位不存在: " + item.getSeatId());
                    }
                    
                    Seat seat = seatOpt.get();
                    if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
                        throw new RuntimeException("座位已被占用: " + item.getSeatId());
                    }
                    
                    // 创建订单详情
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(savedOrder);
                    orderItem.setSeat(seat);
                    orderItem.setPassengerName(item.getPassengerName());
                    orderItem.setPassengerIdCard(item.getPassengerIdCard());
                    orderItem.setPrice(seat.getPrice());
                    orderItem.setCreatedAt(LocalDateTime.now());
                    
                    // 更新座位状态为已预订
                    seat.setStatus(Seat.SeatStatus.RESERVED);
                    seatRepository.save(seat);
                    
                    // 保存订单详情
                    orderItemRepository.save(orderItem);
                    
                    log.info("座位预订成功: seatId={}, orderNumber={}", seat.getId(), order.getOrderNumber());
                    return null;
                });
            } catch (Exception e) {
                log.error("座位预订失败: seatId={}, error={}", item.getSeatId(), e.getMessage());
                // 抛出运行时异常，触发事务回滚
                throw new RuntimeException("座位预订失败: " + e.getMessage());
            }
        }

        // 获取完整订单信息并转换为DTO
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(savedOrder.getId());
        return new OrderDTO(savedOrder, orderItems);
    }

    @Override
    public List<OrderDTO> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);

        // 转换为DTO
        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    return new OrderDTO(order, items);
                })
                .toList();
    }

    @Override
    public OrderDTO getOrderById(Long orderId, Long userId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("订单不存在");
        }

        Order order = orderOpt.get();
        // 检查是否是用户自己的订单
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权访问此订单");
        }

        // 获取订单详情并转换为DTO
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return new OrderDTO(order, orderItems);
    }

    @Override
    @Transactional
    public OrderDTO cancelOrder(Long orderId, Long userId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("订单不存在");
        }

        Order order = orderOpt.get();
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("只能取消待支付的订单");
        }

        // 更新订单状态
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);

        // 释放座位
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : orderItems) {
            Optional<Seat> seatOpt = seatRepository.findById(item.getSeat().getId());
            if (seatOpt.isPresent()) {
                Seat seat = seatOpt.get();
                seat.setStatus(Seat.SeatStatus.AVAILABLE);
                seatRepository.save(seat);
            }
        }

        // 获取更新后的订单详情
        List<OrderItem> updatedItems = orderItemRepository.findByOrderId(orderId);
        return new OrderDTO(updatedOrder, updatedItems);
    }

    @Override
    @Transactional
    public OrderDTO payOrder(Long orderId, Long userId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("订单不存在");
        }

        Order order = orderOpt.get();
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("订单已处理");
        }

        // 更新订单状态
        order.setStatus(Order.OrderStatus.PAID);
        order.setPaymentTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);

        // 更新座位状态为已占用
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : orderItems) {
            Optional<Seat> seatOpt = seatRepository.findById(item.getSeat().getId());
            if (seatOpt.isPresent()) {
                Seat seat = seatOpt.get();
                seat.setStatus(Seat.SeatStatus.OCCUPIED);
                seatRepository.save(seat);
            }
        }

        // 获取更新后的订单详情
        List<OrderItem> updatedItems = orderItemRepository.findByOrderId(orderId);
        return new OrderDTO(updatedOrder, updatedItems);
    }

    @Override
    public String generateOrderNumber() {
        return "ORD" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}