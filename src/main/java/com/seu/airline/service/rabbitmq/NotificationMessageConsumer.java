package com.seu.airline.service.rabbitmq;

import com.seu.airline.config.RabbitMQConfig;
import com.seu.airline.model.rabbitmq.NotificationMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 通知消息消费者
 * 用于处理系统解耦场景下的通知消息
 */
@Slf4j
@Service
public class NotificationMessageConsumer {

    /**
     * 处理通知消息
     * 实现系统解耦，异步发送各种通知
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void processNotificationMessage(Message<NotificationMessage> message, Channel channel,
                                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            NotificationMessage notificationMessage = message.getPayload();
            log.info("接收到通知消息: 类型={}, 接收人={}", 
                    notificationMessage.getNotificationType(), 
                    notificationMessage.getRecipient());
            
            // 根据通知类型处理不同的通知方式
            switch (notificationMessage.getNotificationType()) {
                case "EMAIL":
                    // TODO: 实现邮件发送逻辑
                    sendEmail(notificationMessage);
                    break;
                case "SMS":
                    // TODO: 实现短信发送逻辑
                    sendSms(notificationMessage);
                    break;
                case "PUSH":
                    // TODO: 实现推送消息逻辑
                    sendPush(notificationMessage);
                    break;
                default:
                    log.warn("未知的通知类型: {}", notificationMessage.getNotificationType());
            }
            
            // 手动确认消息已被处理
            channel.basicAck(deliveryTag, false);
            log.info("通知消息处理完成");
            
        } catch (Exception e) {
            log.error("处理通知消息失败", e);
            try {
                // 拒绝消息并重新入队
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                log.error("拒绝通知消息失败", ex);
            }
        }
    }
    
    /**
     * 发送邮件通知
     */
    private void sendEmail(NotificationMessage message) {
        // 模拟邮件发送
        log.info("正在发送邮件到: {}, 主题: {}", message.getRecipient(), message.getTitle());
        // TODO: 集成邮件发送服务
    }
    
    /**
     * 发送短信通知
     */
    private void sendSms(NotificationMessage message) {
        // 模拟短信发送
        log.info("正在发送短信到: {}, 内容: {}", message.getRecipient(), message.getContent());
        // TODO: 集成短信发送服务
    }
    
    /**
     * 发送推送通知
     */
    private void sendPush(NotificationMessage message) {
        // 模拟推送发送
        log.info("正在发送推送通知到用户: {}, 标题: {}", message.getRecipient(), message.getTitle());
        // TODO: 集成推送服务
    }
}