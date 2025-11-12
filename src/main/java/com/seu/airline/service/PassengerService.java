package com.seu.airline.service;

import com.seu.airline.dto.PassengerCreateRequest;
import com.seu.airline.dto.PassengerDTO;
import com.seu.airline.dto.PassengerUpdateRequest;
import com.seu.airline.model.Passenger;
import com.seu.airline.repository.PassengerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 乘客信息服务类
 */
@Service
@Slf4j
public class PassengerService {

    @Autowired
    private PassengerRepository passengerRepository;

    private static final int MAX_PASSENGERS_PER_USER = 20;

    /**
     * 获取用户的所有乘客
     * @param userId 用户ID
     * @return 乘客列表
     */
    public List<PassengerDTO> getUserPassengers(Long userId) {
        log.info("获取用户 {} 的乘客列表", userId);
        List<Passenger> passengers = passengerRepository
                .findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        
        return passengers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取乘客信息（需验证所属用户）
     * @param id 乘客ID
     * @param userId 用户ID
     * @return 乘客DTO
     */
    public PassengerDTO getPassengerById(Long id, Long userId) {
        log.info("获取用户 {} 的乘客 {}", userId, id);
        Passenger passenger = passengerRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("乘客信息不存在或无权访问"));
        
        return convertToDTO(passenger);
    }

    /**
     * 创建新的乘客
     * @param userId 用户ID
     * @param request 创建请求
     * @return 创建的乘客DTO
     */
    @Transactional
    public PassengerDTO createPassenger(Long userId, PassengerCreateRequest request) {
        log.info("用户 {} 创建新的乘客: {}", userId, request.getPassengerName());

        // 检查用户乘客数量限制
        long count = passengerRepository.countByUserId(userId);
        if (count >= MAX_PASSENGERS_PER_USER) {
            throw new RuntimeException("乘客数量已达上限（" + MAX_PASSENGERS_PER_USER + "个）");
        }

        // 检查证件号是否重复
        if (passengerRepository.existsByUserIdAndIdCard(userId, request.getIdCard())) {
            throw new RuntimeException("该证件号的乘客已存在");
        }

        // 如果设置为默认，先取消其他默认乘客
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            passengerRepository.clearDefaultByUserId(userId);
        }

        // 创建实体
        Passenger passenger = new Passenger();
        passenger.setUserId(userId);
        passenger.setPassengerName(request.getPassengerName());
        passenger.setIdType(request.getIdType());
        passenger.setIdCard(request.getIdCard());
        passenger.setPhone(request.getPhone());
        passenger.setEmail(request.getEmail());
        passenger.setPassengerType(request.getPassengerType());
        passenger.setIsDefault(request.getIsDefault());

        // 保存
        Passenger saved = passengerRepository.save(passenger);
        log.info("乘客创建成功，ID: {}", saved.getId());

        return convertToDTO(saved);
    }

    /**
     * 更新乘客信息
     * @param id 乘客ID
     * @param userId 用户ID
     * @param request 更新请求
     * @return 更新后的乘客DTO
     */
    @Transactional
    public PassengerDTO updatePassenger(Long id, Long userId, PassengerUpdateRequest request) {
        log.info("用户 {} 更新乘客 {}", userId, id);

        // 查找并验证所属权
        Passenger passenger = passengerRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("乘客信息不存在或无权访问"));

        // 更新非空字段
        if (request.getPassengerName() != null) {
            passenger.setPassengerName(request.getPassengerName());
        }
        if (request.getIdType() != null) {
            passenger.setIdType(request.getIdType());
        }
        if (request.getIdCard() != null) {
            // 检查证件号是否与其他乘客重复
            boolean exists = passengerRepository
                    .findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                    .stream()
                    .anyMatch(p -> !p.getId().equals(id) && p.getIdCard().equals(request.getIdCard()));
            if (exists) {
                throw new RuntimeException("该证件号的乘客已存在");
            }
            passenger.setIdCard(request.getIdCard());
        }
        if (request.getPhone() != null) {
            passenger.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            passenger.setEmail(request.getEmail());
        }
        if (request.getPassengerType() != null) {
            passenger.setPassengerType(request.getPassengerType());
        }

        // 保存
        Passenger updated = passengerRepository.save(passenger);
        log.info("乘客 {} 更新成功", id);

        return convertToDTO(updated);
    }

    /**
     * 设置默认乘客
     * @param id 乘客ID
     * @param userId 用户ID
     */
    @Transactional
    public void setDefaultPassenger(Long id, Long userId) {
        log.info("用户 {} 设置默认乘客: {}", userId, id);

        // 验证乘客存在且属于该用户
        Passenger passenger = passengerRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("乘客信息不存在或无权访问"));

        // 取消其他默认乘客
        passengerRepository.clearDefaultByUserId(userId);

        // 设置当前乘客为默认
        passenger.setIsDefault(true);
        passengerRepository.save(passenger);

        log.info("默认乘客设置成功");
    }

    /**
     * 删除乘客
     * @param id 乘客ID
     * @param userId 用户ID
     */
    @Transactional
    public void deletePassenger(Long id, Long userId) {
        log.info("用户 {} 删除乘客 {}", userId, id);

        // 查找并验证所属权
        Passenger passenger = passengerRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("乘客信息不存在或无权访问"));

        // 删除
        passengerRepository.delete(passenger);
        log.info("乘客 {} 删除成功", id);
    }

    /**
     * Entity转DTO
     */
    private PassengerDTO convertToDTO(Passenger entity) {
        PassengerDTO dto = new PassengerDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
