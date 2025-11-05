package com.seu.airline.model.rabbitmq;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知消息类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationMessage extends BaseMessage {
    private static final long serialVersionUID = 1L;
    
    private String recipient;
    private String title;
    private String content;
    private String notificationType; // EMAIL, SMS, PUSH
    private String sender;
}