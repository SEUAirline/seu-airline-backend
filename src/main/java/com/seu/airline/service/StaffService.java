package com.seu.airline.service;

import com.seu.airline.model.Flight;
import com.seu.airline.model.Order;
import com.seu.airline.model.OrderItem;
import java.util.List;

public interface StaffService {
    
    // 获取所有航班
    List<Flight> getAllFlights();
    
    // 根据状态获取航班
    List<Flight> getFlightsByStatus(String status);
    
    // 更新航班状态
    Flight updateFlightStatus(Long id, String status);
    
    // 获取航班乘客信息
    List<OrderItem> getFlightPassengers(Long flightId);
    
    // 获取订单详情（包含订单项）
    OrderDetailResponse getOrderDetails(Long orderId);
    
    // 手动取消超时未支付的订单
    Order cancelOverdueOrder(Long orderId);
    
    // 订单详情响应类
    class OrderDetailResponse {
        private Order order;
        private List<OrderItem> items;
        
        public OrderDetailResponse(Order order, List<OrderItem> items) {
            this.order = order;
            this.items = items;
        }
        
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