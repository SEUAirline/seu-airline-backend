package com.seu.airline.controller;

import com.seu.airline.dto.ApiResponse;
import com.seu.airline.dto.SeatDTO;
import com.seu.airline.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seats")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SeatController {

    @Autowired
    private SeatService seatService;

    // 获取航班的所有座位
    @GetMapping("/flight/{flightId}")
    public ApiResponse<List<SeatDTO>> getFlightSeats(@PathVariable Long flightId) {
        List<SeatDTO> seatDTOs = seatService.getFlightSeats(flightId);
        return ApiResponse.success(seatDTOs, "获取座位列表成功");
    }

    // 获取航班的可用座位
    @GetMapping("/flight/{flightId}/available")
    public ApiResponse<List<SeatDTO>> getAvailableSeats(@PathVariable Long flightId) {
        List<SeatDTO> seatDTOs = seatService.getAvailableSeats(flightId);
        return ApiResponse.success(seatDTOs, "获取可用座位列表成功");
    }

    // 获取航班特定类型的可用座位
    @GetMapping("/flight/{flightId}/type/{seatType}/available")
    public ApiResponse<List<SeatDTO>> getAvailableSeatsByType(
            @PathVariable Long flightId,
            @PathVariable String seatType) {
        try {
            List<SeatDTO> seatDTOs = seatService.getAvailableSeatsByType(flightId, seatType);
            return ApiResponse.success(seatDTOs, "获取" + seatType + "舱可用座位成功");
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("无效的座位类型: " + seatType);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // 获取座位详情
    @GetMapping("/{id}")
    public ApiResponse<SeatDTO> getSeatById(@PathVariable Long id) {
        try {
            SeatDTO seatDTO = seatService.getSeatById(id);
            return ApiResponse.success(seatDTO, "获取座位详情成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // 根据航班ID和座位号获取座位
    @GetMapping("/flight/{flightId}/number/{seatNumber}")
    public ApiResponse<SeatDTO> getSeatByFlightAndNumber(
            @PathVariable Long flightId,
            @PathVariable String seatNumber) {
        try {
            SeatDTO seatDTO = seatService.getSeatByFlightAndNumber(flightId, seatNumber);
            return ApiResponse.success(seatDTO, "获取座位详情成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}