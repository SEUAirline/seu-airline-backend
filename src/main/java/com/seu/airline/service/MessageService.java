package com.seu.airline.service;

import com.seu.airline.dto.MessageDTO;
import com.seu.airline.dto.MessageListResponse;
import com.seu.airline.model.Message;
import com.seu.airline.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MessageService {
    
    @Autowired
    private MessageRepository messageRepository;
    
    /**
     * 获取用户消息列表（分页）
     */
    public MessageListResponse getMessages(Long userId, String type, Boolean isRead, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Message> messagePage;
        
        if (type != null && isRead != null) {
            messagePage = messageRepository.findByUserIdAndMessageTypeAndIsReadOrderByPriorityDescCreatedAtDesc(
                userId, type, isRead, pageable);
        } else if (type != null) {
            messagePage = messageRepository.findByUserIdAndMessageTypeOrderByPriorityDescCreatedAtDesc(
                userId, type, pageable);
        } else if (isRead != null) {
            messagePage = messageRepository.findByUserIdAndIsReadOrderByPriorityDescCreatedAtDesc(
                userId, isRead, pageable);
        } else {
            messagePage = messageRepository.findByUserIdOrderByPriorityDescCreatedAtDesc(userId, pageable);
        }
        
        List<MessageDTO> messageDTOs = messagePage.getContent().stream()
            .map(MessageDTO::new)
            .collect(Collectors.toList());
        
        return new MessageListResponse(messageDTOs, messagePage.getTotalElements(), page, pageSize);
    }
    
    /**
     * 获取未读消息数
     */
    public Long getUnreadCount(Long userId) {
        return messageRepository.countByUserIdAndIsRead(userId, false);
    }
    
    /**
     * 获取消息详情
     */
    public Message getMessageById(Long id) {
        return messageRepository.findById(id).orElse(null);
    }
    
    /**
     * 标记消息为已读
     */
    @Transactional
    public void markAsRead(Long id) {
        Message message = messageRepository.findById(id).orElse(null);
        if (message != null && !message.getIsRead()) {
            message.setIsRead(true);
            message.setReadTime(LocalDateTime.now());
            messageRepository.save(message);
            log.info("消息 {} 已标记为已读", id);
        }
    }
    
    /**
     * 标记所有消息为已读
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<Message> unreadMessages = messageRepository.findByUserIdAndIsReadOrderByPriorityDescCreatedAtDesc(
            userId, false, pageable);
        
        LocalDateTime now = LocalDateTime.now();
        unreadMessages.forEach(message -> {
            message.setIsRead(true);
            message.setReadTime(now);
        });
        
        messageRepository.saveAll(unreadMessages.getContent());
        log.info("用户 {} 的所有消息已标记为已读", userId);
    }
    
    /**
     * 删除消息
     */
    @Transactional
    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
        log.info("消息 {} 已删除", id);
    }
    
    /**
     * 批量删除消息
     */
    @Transactional
    public void batchDeleteMessages(List<Long> ids) {
        messageRepository.deleteAllById(ids);
        log.info("批量删除消息：{}", ids);
    }
    
    /**
     * 创建消息（管理员功能）
     */
    @Transactional
    public Message createMessage(Message message) {
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        Message savedMessage = messageRepository.save(message);
        log.info("创建消息：{} -> 用户 {}", message.getTitle(), message.getUserId());
        return savedMessage;
    }
}
