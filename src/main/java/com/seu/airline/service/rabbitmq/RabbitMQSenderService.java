package com.seu.airline.service.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ消息发送服务
 */
@Service
public class RabbitMQSenderService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息到指定交换机和路由键
     */
    public void sendMessage(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    /**
     * 发送消息到指定交换机和路由键，并设置回调
     */
    public void sendMessageWithCallback(String exchange, String routingKey, Object message,
                                       RabbitTemplate.ConfirmCallback confirmCallback) {
        rabbitTemplate.setConfirmCallback(confirmCallback);
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    /**
     * 发送延迟消息
     */
    public void sendDelayedMessage(String exchange, String routingKey, Object message, long delayMillis) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message, m -> {
            m.getMessageProperties().setDelay((int) delayMillis);
            return m;
        });
    }
}