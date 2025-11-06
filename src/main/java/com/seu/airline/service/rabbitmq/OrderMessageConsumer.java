package com.seu.airline.service.rabbitmq;

import com.seu.airline.config.RabbitMQConfig;
import com.seu.airline.controller.OrderController;
import com.seu.airline.model.rabbitmq.OrderMessage;
import com.seu.airline.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单消息消费者
 * 用于处理削峰填谷场景下的订单消息
 */
@Slf4j
@Service
public class OrderMessageConsumer {

    @Autowired
    private OrderService orderService;

    /**
     * 处理订单消息
     * 手动确认模式，确保消息被正确处理
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void processOrderMessage(Message<OrderMessage> message, Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            OrderMessage orderMessage = message.getPayload();
            log.info("接收到订单消息: {}", orderMessage);
            
            // 1. 验证订单数据
            if (!validateOrderMessage(orderMessage)) {
                log.error("订单消息数据验证失败: {}", orderMessage);
                // 拒绝消息并直接丢弃（不重新入队）
                channel.basicNack(deliveryTag, false, false);
                return;
            }
            
            // 2. 转换OrderMessage为OrderRequest
            OrderController.OrderRequest orderRequest = convertToOrderRequest(orderMessage);
            Long userId = Long.parseLong(orderMessage.getUserId());
            
            // 3. 调用服务创建订单
            orderService.createOrder(orderRequest, userId);
            
            // 4. 手动确认消息已被处理
            channel.basicAck(deliveryTag, false);
            log.info("订单消息处理完成，用户ID: {}", orderMessage.getUserId());
            
        } catch (Exception e) {
            log.error("处理订单消息失败", e);
            try {
                // 判断异常类型决定是否重新入队
                // 对于座位已被占用或不存在的情况，不需要重试
                if (e.getMessage() != null && 
                    (e.getMessage().contains("座位已被占用") || e.getMessage().contains("座位不存在"))) {
                    // 拒绝消息并直接丢弃（不重新入队）
                    log.warn("订单处理失败，不需要重试: {}", e.getMessage());
                    channel.basicNack(deliveryTag, false, false);
                } else {
                    // 其他异常可以重新入队尝试
                    log.warn("订单处理失败，将重新入队: {}", e.getMessage());
                    channel.basicNack(deliveryTag, false, true);
                }
            } catch (IOException ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }

    /**
     * 处理死信队列中的订单消息
     */
    @RabbitListener(queues = RabbitMQConfig.DEAD_LETTER_QUEUE)
    public void processDeadLetterMessage(Message<?> message, Channel channel,
                                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.warn("接收到死信消息: {}", message.getPayload());
            
            // 记录死信消息到日志系统
            // 在实际应用中，可以将这些消息存储到数据库或发送告警通知
            // 死信消息不再重试，直接确认
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            log.error("处理死信消息失败", e);
            try {
                // 死信消息不再重试，即使处理失败也确认
                channel.basicAck(deliveryTag, false);
            } catch (IOException ex) {
                log.error("确认死信消息失败", ex);
            }
        }
    }
    
    /**
     * 验证订单消息数据
     */
    private boolean validateOrderMessage(OrderMessage orderMessage) {
        if (orderMessage == null) {
            return false;
        }
        
        // 验证用户ID
        if (orderMessage.getUserId() == null || orderMessage.getUserId().isEmpty()) {
            return false;
        }
        
        // 验证订单详情
        if (orderMessage.getItems() == null || orderMessage.getItems().isEmpty()) {
            return false;
        }
        
        // 验证每个订单项
        for (OrderMessage.OrderItemDetail item : orderMessage.getItems()) {
            if (item.getSeatId() == null || 
                item.getPassengerName() == null || 
                item.getPassengerName().isEmpty() || 
                item.getPassengerIdCard() == null || 
                item.getPassengerIdCard().isEmpty()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 将OrderMessage转换为OrderRequest
     */
    private OrderController.OrderRequest convertToOrderRequest(OrderMessage orderMessage) {
        OrderController.OrderRequest orderRequest = new OrderController.OrderRequest();
        orderRequest.setFlightNumber(orderMessage.getFlightNumber());
        
        // 转换订单项
        List<OrderController.OrderItemRequest> items = orderMessage.getItems().stream()
            .map(itemDetail -> {
                OrderController.OrderItemRequest itemRequest = new OrderController.OrderItemRequest();
                itemRequest.setSeatId(itemDetail.getSeatId());
                itemRequest.setPassengerName(itemDetail.getPassengerName());
                itemRequest.setPassengerIdCard(itemDetail.getPassengerIdCard());
                return itemRequest;
            })
            .collect(Collectors.toList());
        
        orderRequest.setItems(items);
        return orderRequest;
    }
}