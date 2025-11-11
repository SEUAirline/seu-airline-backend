package com.seu.airline.repository;

import com.seu.airline.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * 查询用户的消息列表（分页）
     */
    Page<Message> findByUserIdOrderByPriorityDescCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 查询用户的未读消息列表
     */
    Page<Message> findByUserIdAndIsReadOrderByPriorityDescCreatedAtDesc(Long userId, Boolean isRead, Pageable pageable);
    
    /**
     * 按类型查询用户消息
     */
    Page<Message> findByUserIdAndMessageTypeOrderByPriorityDescCreatedAtDesc(Long userId, String messageType, Pageable pageable);
    
    /**
     * 按类型和已读状态查询
     */
    Page<Message> findByUserIdAndMessageTypeAndIsReadOrderByPriorityDescCreatedAtDesc(
        Long userId, String messageType, Boolean isRead, Pageable pageable);
    
    /**
     * 统计用户的未读消息数
     */
    Long countByUserIdAndIsRead(Long userId, Boolean isRead);
}
