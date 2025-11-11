package com.seu.airline.model.rabbitmq;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 订单消息类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderMessage extends BaseMessage {
    private static final long serialVersionUID = 1L;
    
    private Long orderId;
    private String userId;
    private String flightNumber;
    private String status;
    private Double amount;
    
    // 订单详情列表
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<OrderItemDetail> items;
    
    /**
     * 订单项详情
     */
    @Data
    public static class OrderItemDetail implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Long seatId;
        private String passengerName;
        private String passengerIdCard;
        
        // ✅ Jackson 需要这个无参构造函数
        public OrderItemDetail() {
        }
        public OrderItemDetail(Long seatId, String passengerName, String passengerIdCard) {
            this.seatId = seatId;
            this.passengerName = passengerName;
            this.passengerIdCard = passengerIdCard;
        }
    }
}
