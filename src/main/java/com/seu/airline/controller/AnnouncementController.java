package com.seu.airline.controller;

import com.seu.airline.dto.AnnouncementDTO;
import com.seu.airline.dto.ApiResponse;
import com.seu.airline.model.Announcement;
import com.seu.airline.security.UserDetailsImpl;
import com.seu.airline.service.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器
 * 提供公告查询和已读标记功能
 */
@Slf4j
@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 获取当前生效的公告列表
     * GET /announcements/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<AnnouncementDTO>>> getActiveAnnouncements() {
        try {
            Long userId = getCurrentUserId();
            log.info("用户 {} 请求获取生效公告", userId);
            
            List<AnnouncementDTO> announcements = announcementService.getActiveAnnouncements();
            log.info("成功获取 {} 条生效公告", announcements.size());
            
            return ResponseEntity.ok(new ApiResponse<>(true, announcements, "获取公告成功"));
        } catch (Exception e) {
            log.error("获取生效公告失败", e);
            return ResponseEntity.ok(new ApiResponse<>(false, null, "获取公告失败: " + e.getMessage()));
        }
    }

    /**
     * 获取单个公告详情
     * GET /announcements/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Announcement>> getAnnouncement(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            log.info("用户 {} 请求获取公告详情: announcementId={}", userId, id);
            
            Announcement announcement = announcementService.getAnnouncementById(id);
            if (announcement == null) {
                log.warn("公告不存在: announcementId={}", id);
                return ResponseEntity.ok(new ApiResponse<>(false, null, "公告不存在"));
            }
            
            log.info("成功获取公告详情: announcementId={}", id);
            return ResponseEntity.ok(new ApiResponse<>(true, announcement, "获取公告成功"));
        } catch (Exception e) {
            log.error("获取公告详情失败: announcementId={}", id, e);
            return ResponseEntity.ok(new ApiResponse<>(false, null, "获取公告失败: " + e.getMessage()));
        }
    }

    /**
     * 标记公告为已读
     * PUT /announcements/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAnnouncementAsRead(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            log.info("用户 {} 标记公告为已读: announcementId={}", userId, id);
            
            announcementService.markAnnouncementAsRead(id, userId);
            log.info("成功标记公告为已读: userId={}, announcementId={}", userId, id);
            
            return ResponseEntity.ok(new ApiResponse<>(true, null, "标记已读成功"));
        } catch (Exception e) {
            log.error("标记公告已读失败: userId={}, announcementId={}", getCurrentUserId(), id, e);
            return ResponseEntity.ok(new ApiResponse<>(false, null, "标记已读失败: " + e.getMessage()));
        }
    }

    /**
     * 获取当前登录用户ID
     * 从Spring Security上下文中提取用户ID
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return userDetails.getId();
    }
}
