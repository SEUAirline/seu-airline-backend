package com.seu.airline.dto;

import com.seu.airline.model.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String type;
    private Long relatedId;
    private Integer priority;
    private Boolean isRead;
    private LocalDateTime createTime;
    private LocalDateTime readTime;
    
    public MessageDTO(Message message) {
        this.id = message.getId();
        this.userId = message.getUserId();
        this.title = message.getTitle();
        this.content = message.getContent();
        this.type = message.getMessageType();
        this.relatedId = message.getRelatedId();
        this.priority = message.getPriority();
        this.isRead = message.getIsRead();
        this.createTime = message.getCreatedAt();
        this.readTime = message.getReadTime();
    }
}
