package com.seu.airline.model.rabbitmq;

import lombok.Data;
import lombok.EqualsAndHashCode;

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
}
