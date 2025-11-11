package com.seu.airline.dto;

import com.seu.airline.model.Announcement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementDTO {
    private Long id;
    private String title;
    private String content;
    private String type;
    private Integer priority;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private LocalDateTime createTime;
    
    public AnnouncementDTO(Announcement announcement) {
        this.id = announcement.getId();
        this.title = announcement.getTitle();
        this.content = announcement.getContent();
        this.type = announcement.getAnnouncementType();
        this.priority = announcement.getPriority();
        this.startTime = announcement.getStartTime();
        this.endTime = announcement.getEndTime();
        this.status = announcement.getStatus();
        this.createTime = announcement.getCreatedAt();
    }
}
