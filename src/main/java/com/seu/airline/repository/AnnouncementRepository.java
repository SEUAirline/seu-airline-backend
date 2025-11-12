package com.seu.airline.repository;

import com.seu.airline.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    
    /**
     * 查询当前有效的公告（在时间范围内且状态启用）
     */
    @Query("SELECT a FROM Announcement a WHERE a.status = 1 " +
           "AND a.startTime <= :now AND a.endTime >= :now " +
           "ORDER BY a.priority DESC, a.createdAt DESC")
    List<Announcement> findActiveAnnouncements(@Param("now") LocalDateTime now);
}
