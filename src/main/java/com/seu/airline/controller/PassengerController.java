package com.seu.airline.controller;

import com.seu.airline.dto.ApiResponse;
import com.seu.airline.dto.PassengerCreateRequest;
import com.seu.airline.dto.PassengerDTO;
import com.seu.airline.dto.PassengerUpdateRequest;
import com.seu.airline.security.UserDetailsImpl;
import com.seu.airline.service.PassengerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 乘客信息管理控制器
 */
@RestController
@RequestMapping("/passengers")
@Api(tags = "乘客信息管理")
@Slf4j
@Validated
public class PassengerController {

    @Autowired
    private PassengerService passengerService;

    /**
     * 获取当前用户的所有乘客
     */
    @GetMapping
    @ApiOperation("获取乘客列表")
    public ResponseEntity<ApiResponse<List<PassengerDTO>>> getPassengers() {
        Long userId = getCurrentUserId();
        log.info("用户 {} 请求乘客列表", userId);

        List<PassengerDTO> passengers = passengerService.getUserPassengers(userId);
        return ResponseEntity.ok(ApiResponse.success(passengers));
    }

    /**
     * 根据ID获取乘客详情
     */
    @GetMapping("/{id}")
    @ApiOperation("获取乘客详情")
    public ResponseEntity<ApiResponse<PassengerDTO>> getPassengerById(
            @ApiParam(value = "乘客ID", required = true) @PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("用户 {} 请求乘客 {} 的详情", userId, id);

        PassengerDTO passenger = passengerService.getPassengerById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(passenger));
    }

    /**
     * 创建新的乘客
     */
    @PostMapping
    @ApiOperation("创建乘客")
    public ResponseEntity<ApiResponse<PassengerDTO>> createPassenger(
            @ApiParam(value = "创建请求", required = true) @Valid @RequestBody PassengerCreateRequest request) {
        Long userId = getCurrentUserId();
        log.info("用户 {} 创建乘客: {}", userId, request.getPassengerName());

        PassengerDTO created = passengerService.createPassenger(userId, request);
        return ResponseEntity.ok(ApiResponse.success(created, "乘客创建成功"));
    }

    /**
     * 更新乘客信息
     */
    @PutMapping("/{id}")
    @ApiOperation("更新乘客")
    public ResponseEntity<ApiResponse<PassengerDTO>> updatePassenger(
            @ApiParam(value = "乘客ID", required = true) @PathVariable Long id,
            @ApiParam(value = "更新请求", required = true) @Valid @RequestBody PassengerUpdateRequest request) {
        Long userId = getCurrentUserId();
        log.info("用户 {} 更新乘客 {}", userId, id);

        PassengerDTO updated = passengerService.updatePassenger(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "乘客更新成功"));
    }

    /**
     * 设置默认乘客
     */
    @PatchMapping("/{id}/default")
    @ApiOperation("设置默认乘客")
    public ResponseEntity<ApiResponse<Void>> setDefaultPassenger(
            @ApiParam(value = "乘客ID", required = true) @PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("用户 {} 设置默认乘客: {}", userId, id);

        passengerService.setDefaultPassenger(id, userId);
        return ResponseEntity.ok(ApiResponse.success("默认乘客设置成功"));
    }

    /**
     * 删除乘客
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除乘客")
    public ResponseEntity<ApiResponse<Void>> deletePassenger(
            @ApiParam(value = "乘客ID", required = true) @PathVariable Long id) {
        Long userId = getCurrentUserId();
        log.info("用户 {} 删除乘客 {}", userId, id);

        passengerService.deletePassenger(id, userId);
        return ResponseEntity.ok(ApiResponse.success("乘客删除成功"));
    }

    /**
     * 业务异常处理
     * 处理如资源不存在、权限不足等业务层异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        // 业务异常使用 warn 级别，不会影响服务运行
        log.warn("业务异常: {}", e.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
    
    /**
     * 通用异常处理
     * 捕获未预期的系统异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        // 系统异常使用 error 级别，并记录堆栈跟踪
        log.error("系统异常: {}", e.getMessage(), e);
        return ResponseEntity.status(500).body(ApiResponse.error("服务器内部错误，请稍后重试"));
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
