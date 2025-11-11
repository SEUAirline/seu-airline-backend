package com.seu.airline.repository;

import com.seu.airline.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 乘客信息数据访问层
 */
@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    /**
     * 根据用户ID查找所有乘客（默认乘客在前，按创建时间倒序）
     * @param userId 用户ID
     * @return 乘客列表
     */
    List<Passenger> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);

    /**
     * 根据用户ID和乘客ID查找乘客
     * @param id 乘客ID
     * @param userId 用户ID
     * @return 乘客信息
     */
    Optional<Passenger> findByIdAndUserId(Long id, Long userId);

    /**
     * 根据用户ID删除所有乘客
     * @param userId 用户ID
     */
    void deleteByUserId(Long userId);

    /**
     * 查询用户的默认乘客
     * @param userId 用户ID
     * @return 默认乘客
     */
    Optional<Passenger> findByUserIdAndIsDefaultTrue(Long userId);

    /**
     * 取消用户所有乘客的默认状态
     * @param userId 用户ID
     */
    @Modifying
    @Query("UPDATE Passenger p SET p.isDefault = false WHERE p.userId = :userId")
    void clearDefaultByUserId(@Param("userId") Long userId);

    /**
     * 统计用户的乘客数量
     * @param userId 用户ID
     * @return 数量
     */
    long countByUserId(Long userId);

    /**
     * 检查用户是否已保存该证件号的乘客
     * @param userId 用户ID
     * @param idCard 证件号码
     * @return 是否存在
     */
    boolean existsByUserIdAndIdCard(Long userId, String idCard);
}
