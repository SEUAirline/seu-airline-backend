package com.seu.airline.model.rabbitmq;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 日志消息类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LogMessage extends BaseMessage {
    private static final long serialVersionUID = 1L;
    
    private String level;
    private String className;
    private String methodName;
    private String content;
    private String ip;
    private String userId;
}