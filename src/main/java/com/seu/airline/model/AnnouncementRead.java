package com.seu.airline.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcement_reads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementRead {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "announcement_id", nullable = false)
    private Long announcementId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "read_time", nullable = false)
    private LocalDateTime readTime = LocalDateTime.now();
}
