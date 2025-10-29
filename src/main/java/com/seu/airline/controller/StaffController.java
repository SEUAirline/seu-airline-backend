package com.seu.airline.controller;

import com.seu.airline.dto.ApiResponse;
import com.seu.airline.model.Flight;
import com.seu.airline.model.Order;
import com.seu.airline.model.OrderItem;
import com.seu.airline.model.Seat;
import com.seu.airline.repository.FlightRepository;
import com.seu.airline.repository.OrderItemRepository;
import com.seu.airline.repository.OrderRepository;
import com.seu.airline.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/staff")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StaffController {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private SeatRepository seatRepository;

    // 获取所有航班
    @GetMapping("/flights")
    public ResponseEntity<?> getAllFlights() {
        List<Flight> flights = flightRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(flights, "获取航班列表成功"));
    }

    // 获取特定状态的航班
    @GetMapping("/flights/status/{status}")
    public ResponseEntity<?> getFlightsByStatus(@PathVariable String status) {
        try {
            Flight.FlightStatus flightStatus = Flight.FlightStatus.valueOf(status.toUpperCase());
            List<Flight> flights = flightRepository.findByStatus(flightStatus);
            return ResponseEntity.ok(ApiResponse.success(flights, "获取" + status + "状态航班成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("无效的航班状态: " + status));
        }
    }

    // 更新航班状态
    @PutMapping("/flights/{id}/status")
    public ResponseEntity<?> updateFlightStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Optional<Flight> flightOpt = flightRepository.findById(id);
        if (!flightOpt.isPresent()) {
            return ResponseEntity.status(404).body(ApiResponse.error("航班不存在"));
        }

        try {
            Flight flight = flightOpt.get();
            Flight.FlightStatus newStatus = Flight.FlightStatus.valueOf(status.toUpperCase());
            flight.setStatus(newStatus);
            flight.setUpdatedAt(LocalDateTime.now());
            flightRepository.save(flight);

            return ResponseEntity.ok(ApiResponse.success(flight, "航班状态更新成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("无效的航班状态: " + status));
        }
    }

    // 获取航班乘客信息
    @GetMapping("/flights/{id}/passengers")
    public ResponseEntity<?> getFlightPassengers(@PathVariable Long id) {
        Optional<Flight> flightOpt = flightRepository.findById(id);
        if (!flightOpt.isPresent()) {
            return ResponseEntity.status(404).body(ApiResponse.error("航班不存在"));
        }

        // 获取航班的所有座位
        List<Seat> seats = seatRepository.findByFlightId(id);

        // 获取已被占用的座位的订单信息
        List<OrderItem> passengers = new java.util.ArrayList<>();
        for (Seat seat : seats) {
            if (seat.getStatus() == Seat.SeatStatus.OCCUPIED) {
                List<OrderItem> items = orderItemRepository.findBySeatId(seat.getId());
                passengers.addAll(items);
            }
        }

        return ResponseEntity.ok(ApiResponse.success(passengers, "获取航班乘客信息成功"));
    }

    // 查看订单详情（用于核对乘客信息）
    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getOrderDetails(@PathVariable Long id) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (!orderOpt.isPresent()) {
            return ResponseEntity.status(404).body(ApiResponse.error("订单不存在"));
        }

        Order order = orderOpt.get();
        List<OrderItem> items = orderItemRepository.findByOrderId(id);

        OrderDetailResponse response = new OrderDetailResponse(order, items);
        return ResponseEntity.ok(ApiResponse.success(response, "获取订单详情成功"));
    }

    // 手动取消超时未支付的订单
    @PutMapping("/orders/{id}/cancel-overdue")
    public ResponseEntity<?> cancelOverdueOrder(@PathVariable Long id) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (!orderOpt.isPresent()) {
            return ResponseEntity.status(404).body(ApiResponse.error("订单不存在"));
        }

        Order order = orderOpt.get();
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            return ResponseEntity.badRequest().body(ApiResponse.error("只能取消待支付的订单"));
        }

        // 检查是否超过支付时间（例如30分钟）
        if (order.getCreatedAt().plusMinutes(30).isAfter(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("订单未超时，不能强制取消"));
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

        return ResponseEntity.ok(ApiResponse.success(order, "超时订单已取消"));
    }

    // 响应类
    public static class OrderDetailResponse {
        private Order order;
        private List<OrderItem> items;

        public OrderDetailResponse(Order order, List<OrderItem> items) {
            this.order = order;
            this.items = items;
        }

        // getters and setters
        public Order getOrder() {
            return order;
        }

        public void setOrder(Order order) {
            this.order = order;
        }

        public List<OrderItem> getItems() {
            return items;
        }

        public void setItems(List<OrderItem> items) {
            this.items = items;
        }
    }
}