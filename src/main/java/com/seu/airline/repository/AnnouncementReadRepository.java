package com.seu.airline.repository;

import com.seu.airline.model.AnnouncementRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnnouncementReadRepository extends JpaRepository<AnnouncementRead, Long> {
    
    /**
     * 查询用户对某个公告的阅读记录
     */
    Optional<AnnouncementRead> findByAnnouncementIdAndUserId(Long announcementId, Long userId);
    
    /**
     * 检查用户是否已读某个公告
     */
    boolean existsByAnnouncementIdAndUserId(Long announcementId, Long userId);
}
