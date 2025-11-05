package com.seu.airline.service.impl;

import com.seu.airline.model.Flight;
import com.seu.airline.model.Order;
import com.seu.airline.model.OrderItem;
import com.seu.airline.model.Seat;
import com.seu.airline.repository.FlightRepository;
import com.seu.airline.repository.OrderItemRepository;
import com.seu.airline.repository.OrderRepository;
import com.seu.airline.repository.SeatRepository;
import com.seu.airline.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StaffServiceImpl implements StaffService {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Override
    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    @Override
    public List<Flight> getFlightsByStatus(String status) {
        Flight.FlightStatus flightStatus = Flight.FlightStatus.valueOf(status.toUpperCase());
        return flightRepository.findByStatus(flightStatus);
    }

    @Override
    @Transactional
    public Flight updateFlightStatus(Long id, String status) {
        Optional<Flight> flightOpt = flightRepository.findById(id);
        if (!flightOpt.isPresent()) {
            throw new RuntimeException("航班不存在");
        }

        Flight flight = flightOpt.get();
        Flight.FlightStatus newStatus = Flight.FlightStatus.valueOf(status.toUpperCase());
        flight.setStatus(newStatus);
        flight.setUpdatedAt(LocalDateTime.now());
        return flightRepository.save(flight);
    }

    @Override
    public List<OrderItem> getFlightPassengers(Long flightId) {
        Optional<Flight> flightOpt = flightRepository.findById(flightId);
        if (!flightOpt.isPresent()) {
            throw new RuntimeException("航班不存在");
        }

        // 获取航班的所有座位
        List<Seat> seats = seatRepository.findByFlightId(flightId);

        // 获取已被占用的座位的订单信息
        List<OrderItem> passengers = new ArrayList<>();
        for (Seat seat : seats) {
            if (seat.getStatus() == Seat.SeatStatus.OCCUPIED) {
                List<OrderItem> items = orderItemRepository.findBySeatId(seat.getId());
                passengers.addAll(items);
            }
        }

        return passengers;
    }

    @Override
    public OrderDetailResponse getOrderDetails(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("订单不存在");
        }

        Order order = orderOpt.get();
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        return new OrderDetailResponse(order, items);
    }

    @Override
    @Transactional
    public Order cancelOverdueOrder(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("订单不存在");
        }

        Order order = orderOpt.get();
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("只能取消待支付的订单");
        }

        // 检查是否超过支付时间（例如30分钟）
        if (order.getCreatedAt().plusMinutes(30).isAfter(LocalDateTime.now())) {
            throw new RuntimeException("订单未超时，不能强制取消");
        }

        // 更新订单状态
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

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

        return order;
    }
}