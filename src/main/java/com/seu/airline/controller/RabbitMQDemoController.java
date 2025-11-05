package com.seu.airline.controller;

import com.seu.airline.config.RabbitMQConfig;
import com.seu.airline.model.rabbitmq.LogMessage;
import com.seu.airline.model.rabbitmq.OrderMessage;
import com.seu.airline.model.rabbitmq.NotificationMessage;
import com.seu.airline.service.rabbitmq.RabbitMQSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * RabbitMQ演示控制器
 * 用于演示如何发送各类消息到RabbitMQ
 */
@Slf4j
@RestController
@RequestMapping("/rabbitmq")
public class RabbitMQDemoController {

    @Autowired
    private RabbitMQSenderService rabbitMQSenderService;

    /**
     * 发送订单消息（削峰填谷示例）
     */
    @PostMapping("/order")
    public ResponseEntity<String> sendOrderMessage(@RequestBody OrderMessage orderMessage) {
        try {
            log.info("Received order message: {}", orderMessage);
            
            // 设置消息基本信息
            orderMessage.setMessageId(UUID.randomUUID().toString());
            orderMessage.setSource("API");
            orderMessage.setTimestamp(LocalDateTime.now());
            orderMessage.setType("ORDER_CREATE");
            
            log.info("Sending message to RabbitMQ: exchange={}, routingKey={}", 
                    RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.ORDER_ROUTING_KEY);
            
            // 发送到订单队列
            rabbitMQSenderService.sendMessage(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_ROUTING_KEY,
                    orderMessage
            );
            
            log.info("Order message sent successfully");
            return ResponseEntity.ok("订单消息已发送到队列，等待处理");
        } catch (Exception e) {
            log.error("Failed to send order message to RabbitMQ", e);
            throw e;
        }
    }

    /**
     * 发送日志消息（日志数据采集示例）
     */
    @PostMapping("/log")
    public ResponseEntity<String> sendLogMessage(@RequestBody LogMessage logMessage) {
        // 设置消息基本信息
        logMessage.setMessageId(UUID.randomUUID().toString());
        logMessage.setSource("API");
        logMessage.setTimestamp(LocalDateTime.now());
        logMessage.setType("APPLICATION_LOG");
        
        // 发送到日志队列
        rabbitMQSenderService.sendMessage(
                RabbitMQConfig.LOG_EXCHANGE,
                RabbitMQConfig.LOG_ROUTING_KEY,
                logMessage
        );
        
        return ResponseEntity.ok("日志消息已发送到队列，等待大数据分析处理");
    }

    /**
     * 发送通知消息（系统解耦示例）
     */
    @PostMapping("/notification")
    public ResponseEntity<String> sendNotificationMessage(@RequestBody NotificationMessage notificationMessage) {
        // 设置消息基本信息
        notificationMessage.setMessageId(UUID.randomUUID().toString());
        notificationMessage.setSource("API");
        notificationMessage.setTimestamp(LocalDateTime.now());
        notificationMessage.setType("USER_NOTIFICATION");
        
        // 发送到通知队列
        rabbitMQSenderService.sendMessage(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                notificationMessage
        );
        
        return ResponseEntity.ok("通知消息已发送到队列，将异步处理发送");
    }
}