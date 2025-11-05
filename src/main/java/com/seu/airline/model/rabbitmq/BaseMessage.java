package com.seu.airline.model.rabbitmq;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础消息类
 */
@Data
public class BaseMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String messageId;
    private String source;
    private LocalDateTime timestamp= LocalDateTime.now();
    private String type;
}
