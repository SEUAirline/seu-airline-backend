package com.seu.airline.service;

import com.seu.airline.dto.AnnouncementDTO;
import com.seu.airline.model.Announcement;
import com.seu.airline.model.AnnouncementRead;
import com.seu.airline.repository.AnnouncementReadRepository;
import com.seu.airline.repository.AnnouncementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AnnouncementService {
    
    @Autowired
    private AnnouncementRepository announcementRepository;
    
    @Autowired
    private AnnouncementReadRepository announcementReadRepository;
    
    /**
     * 获取当前有效的公告列表
     */
    public List<AnnouncementDTO> getActiveAnnouncements() {
        List<Announcement> announcements = announcementRepository.findActiveAnnouncements(LocalDateTime.now());
        return announcements.stream()
            .map(AnnouncementDTO::new)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取公告详情
     */
    public Announcement getAnnouncementById(Long id) {
        return announcementRepository.findById(id).orElse(null);
    }
    
    /**
     * 标记公告为已读
     */
    @Transactional
    public void markAnnouncementAsRead(Long announcementId, Long userId) {
        // 检查是否已存在阅读记录
        if (!announcementReadRepository.existsByAnnouncementIdAndUserId(announcementId, userId)) {
            AnnouncementRead read = new AnnouncementRead();
            read.setAnnouncementId(announcementId);
            read.setUserId(userId);
            read.setReadTime(LocalDateTime.now());
            announcementReadRepository.save(read);
            log.info("用户 {} 已阅读公告 {}", userId, announcementId);
        }
    }
    
    /**
     * 检查用户是否已读某个公告
     */
    public boolean hasUserReadAnnouncement(Long announcementId, Long userId) {
        return announcementReadRepository.existsByAnnouncementIdAndUserId(announcementId, userId);
    }
}
