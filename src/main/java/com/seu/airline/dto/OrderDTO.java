package com.seu.airline.dto;

import com.seu.airline.model.Order;
import com.seu.airline.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private String id;
    private String orderNumber;
    private Long userId;
    private String flightId;
    private String flightNo;
    private String departureCity;
    private String arrivalCity;
    private String departureTime;
    private String arrivalTime;
    private String date;
    private List<PassengerDTO> passengers;
    private String cabinClass;
    private Double price;
    private Double totalAmount;
    private String status;
    private String paymentMethod;
    private String createTime;
    private String payTime;
    private String contactName;
    private String contactPhone;
    private String contactEmail;

    public OrderDTO(Order order, List<OrderItem> orderItems) {
        this.id = order.getId() != null ? order.getId().toString() : null;
        this.orderNumber = order.getOrderNumber();
        this.userId = order.getUser() != null ? order.getUser().getId() : null;
        this.totalAmount = order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0;
        this.status = order.getStatus() != null ? order.getStatus().name().toLowerCase() : "pending";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.createTime = order.getCreatedAt() != null ? order.getCreatedAt().format(formatter) : null;
        this.payTime = order.getPaymentTime() != null ? order.getPaymentTime().format(formatter) : null;

        // 从订单项中提取信息
        if (orderItems != null && !orderItems.isEmpty()) {
            OrderItem firstItem = orderItems.get(0);
            if (firstItem.getSeat() != null && firstItem.getSeat().getFlight() != null) {
                var flight = firstItem.getSeat().getFlight();
                this.flightId = flight.getId() != null ? flight.getId().toString() : null;
                this.flightNo = flight.getFlightNumber();

                if (flight.getDepartureAirport() != null) {
                    this.departureCity = flight.getDepartureAirport().getCity();
                }
                if (flight.getArrivalAirport() != null) {
                    this.arrivalCity = flight.getArrivalAirport().getCity();
                }

                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                if (flight.getDepartureTime() != null) {
                    this.departureTime = flight.getDepartureTime().format(timeFormatter);
                    this.date = flight.getDepartureTime().format(dateFormatter);
                }
                if (flight.getArrivalTime() != null) {
                    this.arrivalTime = flight.getArrivalTime().format(timeFormatter);
                }
            }

            // 转换乘客信息
            this.passengers = orderItems.stream()
                    .map(item -> new PassengerDTO(
                            item.getPassengerName(),
                            item.getPassengerIdCard(),
                            null, // phone
                            "adult" // passengerType
                    ))
                    .collect(Collectors.toList());

            // 获取舱位类型（从第一个座位获取）
            if (firstItem.getSeat() != null && firstItem.getSeat().getSeatType() != null) {
                this.cabinClass = firstItem.getSeat().getSeatType().name().toLowerCase();
            }

            // 计算单价（总金额除以乘客数）
            if (!orderItems.isEmpty()) {
                this.price = this.totalAmount / orderItems.size();
            }
        } else {
            this.passengers = new ArrayList<>();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassengerDTO {
        private String name;
        private String idCard;
        private String phone;
        private String passengerType;
    }
}
