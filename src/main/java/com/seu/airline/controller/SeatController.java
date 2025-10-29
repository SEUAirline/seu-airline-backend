package com.seu.airline.controller;

import com.seu.airline.dto.ApiResponse;
import com.seu.airline.dto.SeatDTO;
import com.seu.airline.model.Seat;
import com.seu.airline.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/seats")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SeatController {

    @Autowired
    private SeatRepository seatRepository;

    // 获取航班的所有座位
    @GetMapping("/flight/{flightId}")
    public ApiResponse<List<SeatDTO>> getFlightSeats(@PathVariable Long flightId) {
        List<Seat> seats = seatRepository.findByFlightId(flightId);
        List<SeatDTO> seatDTOs = seats.stream()
                .map(SeatDTO::fromEntity)
                .collect(Collectors.toList());
        return ApiResponse.success(seatDTOs, "获取座位列表成功");
    }

    // 获取航班的可用座位
    @GetMapping("/flight/{flightId}/available")
    public ApiResponse<List<SeatDTO>> getAvailableSeats(@PathVariable Long flightId) {
        List<Seat> seats = seatRepository.findByFlightIdAndStatus(
                flightId,
                Seat.SeatStatus.AVAILABLE);
        List<SeatDTO> seatDTOs = seats.stream()
                .map(SeatDTO::fromEntity)
                .collect(Collectors.toList());
        return ApiResponse.success(seatDTOs, "获取可用座位列表成功");
    }

    // 获取航班特定类型的可用座位
    @GetMapping("/flight/{flightId}/type/{seatType}/available")
    public ApiResponse<List<SeatDTO>> getAvailableSeatsByType(
            @PathVariable Long flightId,
            @PathVariable String seatType) {
        try {
            Seat.SeatType type = Seat.SeatType.valueOf(seatType.toUpperCase());
            List<Seat> seats = seatRepository.findByFlightIdAndSeatTypeAndStatus(
                    flightId,
                    type,
                    Seat.SeatStatus.AVAILABLE);
            List<SeatDTO> seatDTOs = seats.stream()
                    .map(SeatDTO::fromEntity)
                    .collect(Collectors.toList());
            return ApiResponse.success(seatDTOs, "获取" + seatType + "舱可用座位成功");
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("无效的座位类型: " + seatType);
        }
    }

    // 获取座位详情
    @GetMapping("/{id}")
    public ApiResponse<SeatDTO> getSeatById(@PathVariable Long id) {
        return seatRepository.findById(id)
                .map(seat -> ApiResponse.success(SeatDTO.fromEntity(seat), "获取座位详情成功"))
                .orElse(ApiResponse.error("座位不存在"));
    }

    // 根据航班ID和座位号获取座位
    @GetMapping("/flight/{flightId}/number/{seatNumber}")
    public ApiResponse<SeatDTO> getSeatByFlightAndNumber(
            @PathVariable Long flightId,
            @PathVariable String seatNumber) {
        return seatRepository.findByFlightIdAndSeatNumber(flightId, seatNumber)
                .map(seat -> ApiResponse.success(SeatDTO.fromEntity(seat), "获取座位详情成功"))
                .orElse(ApiResponse.error("座位不存在"));
    }
}