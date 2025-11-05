package com.seu.airline.service.rabbitmq;

import com.seu.airline.config.RabbitMQConfig;
import com.seu.airline.model.rabbitmq.OrderMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 订单消息消费者
 * 用于处理削峰填谷场景下的订单消息
 */
@Slf4j
@Service
public class OrderMessageConsumer {

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
            
            // TODO: 实现订单处理逻辑
            // 1. 验证订单数据
            // 2. 保存订单到数据库
            // 3. 扣减库存
            // 4. 生成支付记录
            
            // 模拟处理时间
            Thread.sleep(100);
            
            // 手动确认消息已被处理
            channel.basicAck(deliveryTag, false);
            log.info("订单消息处理完成，订单ID: {}", orderMessage.getOrderId());
            
        } catch (Exception e) {
            log.error("处理订单消息失败", e);
            try {
                // 拒绝消息并重新入队
                channel.basicNack(deliveryTag, false, false);
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
            // TODO: 实现死信消息处理逻辑
            // 1. 记录死信消息到日志
            // 2. 可能的重试机制或告警
            
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理死信消息失败", e);
            try {
                channel.basicAck(deliveryTag, false); // 死信消息不再重试
            } catch (IOException ex) {
                log.error("确认死信消息失败", ex);
            }
        }
    }
}