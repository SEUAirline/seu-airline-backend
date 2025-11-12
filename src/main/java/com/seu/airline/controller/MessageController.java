package com.seu.airline.controller;

import com.seu.airline.dto.ApiResponse;
import com.seu.airline.dto.MessageDTO;
import com.seu.airline.dto.MessageListResponse;
import com.seu.airline.model.Message;
import com.seu.airline.security.UserDetailsImpl;
import com.seu.airline.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消息控制器
 * 注意：全局已有 /api 前缀，这里只需要 /messages
 */
@RestController
@RequestMapping("/messages")
@Slf4j
public class MessageController {
    
    @Autowired
    private MessageService messageService;
    
    /**
     * 获取当前用户的消息列表（分页）
     * GET /api/messages?type=ORDER&isRead=false&page=1&pageSize=10
     */
    @GetMapping
    public ResponseEntity<ApiResponse<MessageListResponse>> getMessages(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Long userId = getCurrentUserId();
            MessageListResponse response = messageService.getMessages(userId, type, isRead, page, pageSize);
            return ResponseEntity.ok(ApiResponse.success(response, "获取消息列表成功"));
        } catch (Exception e) {
            log.error("获取消息列表失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取消息列表失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取未读消息数
     * GET /api/messages/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        try {
            Long userId = getCurrentUserId();
            Long count = messageService.getUnreadCount(userId);
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("获取未读消息数失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取未读消息数失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取消息详情
     * GET /api/messages/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MessageDTO>> getMessageDetail(@PathVariable Long id) {
        try {
            Message message = messageService.getMessageById(id);
            if (message == null) {
                return ResponseEntity.ok(ApiResponse.error("消息不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success(new MessageDTO(message)));
        } catch (Exception e) {
            log.error("获取消息详情失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取消息详情失败：" + e.getMessage()));
        }
    }
    
    /**
     * 标记消息为已读
     * PUT /api/messages/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        try {
            messageService.markAsRead(id);
            return ResponseEntity.ok(ApiResponse.success("标记已读成功"));
        } catch (Exception e) {
            log.error("标记消息已读失败", e);
            return ResponseEntity.ok(ApiResponse.error("标记已读失败：" + e.getMessage()));
        }
    }
    
    /**
     * 标记所有消息为已读
     * PUT /api/messages/read-all
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        try {
            Long userId = getCurrentUserId();
            messageService.markAllAsRead(userId);
            return ResponseEntity.ok(ApiResponse.success("全部标记已读成功"));
        } catch (Exception e) {
            log.error("标记全部消息已读失败", e);
            return ResponseEntity.ok(ApiResponse.error("全部标记已读失败：" + e.getMessage()));
        }
    }
    
    /**
     * 删除消息
     * DELETE /api/messages/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long id) {
        try {
            messageService.deleteMessage(id);
            return ResponseEntity.ok(ApiResponse.success("删除消息成功"));
        } catch (Exception e) {
            log.error("删除消息失败", e);
            return ResponseEntity.ok(ApiResponse.error("删除消息失败：" + e.getMessage()));
        }
    }
    
    /**
     * 批量删除消息
     * DELETE /api/messages/batch
     * Body: { "ids": [1, 2, 3] }
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> batchDeleteMessages(@RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> ids = request.get("ids");
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("请提供要删除的消息ID列表"));
            }
            messageService.batchDeleteMessages(ids);
            return ResponseEntity.ok(ApiResponse.success("批量删除消息成功"));
        } catch (Exception e) {
            log.error("批量删除消息失败", e);
            return ResponseEntity.ok(ApiResponse.error("批量删除消息失败：" + e.getMessage()));
        }
    }
    
    /**
     * 创建消息（管理员功能）
     * POST /api/messages
     * 注意：如果请求体未提供userId，则使用当前登录用户ID
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MessageDTO>> createMessage(@RequestBody Message message) {
        try {
            // 如果未指定userId，使用当前登录用户ID
            if (message.getUserId() == null) {
                message.setUserId(getCurrentUserId());
            }
            
            log.info("用户 {} 创建消息: {}", getCurrentUserId(), message.getTitle());
            Message created = messageService.createMessage(message);
            return ResponseEntity.ok(ApiResponse.success(new MessageDTO(created), "创建消息成功"));
        } catch (Exception e) {
            log.error("创建消息失败", e);
            return ResponseEntity.ok(ApiResponse.error("创建消息失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getId();
        }
        throw new RuntimeException("用户未登录");
    }
}
