package com.seu.airline.service.rabbitmq;

import com.seu.airline.config.RabbitMQConfig;
import com.seu.airline.model.rabbitmq.LogMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 日志消息消费者
 * 用于处理日志数据采集场景下的日志消息
 */
@Slf4j
@Service
public class LogMessageConsumer {

    /**
     * 处理日志消息
     * 用于大数据分析和日志存储
     */
    @RabbitListener(queues = RabbitMQConfig.LOG_QUEUE)
    public void processLogMessage(Message<LogMessage> message, Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            LogMessage logMessage = message.getPayload();
            log.info("接收到日志消息: 级别={}, 内容={}", logMessage.getLevel(), logMessage.getContent());
            
            // TODO: 实现日志处理逻辑
            // 1. 根据日志级别进行分类处理
            // 2. 批量存储日志到数据库或文件系统
            // 3. 对于ERROR级别日志进行告警处理
            // 4. 为大数据分析准备数据
            
            // 模拟处理时间
            Thread.sleep(50);
            
            // 手动确认消息已被处理
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            log.error("处理日志消息失败", e);
            try {
                // 拒绝消息并重新入队
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                log.error("拒绝日志消息失败", ex);
            }
        }
    }
}