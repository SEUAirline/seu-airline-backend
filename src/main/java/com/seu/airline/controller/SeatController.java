package com.seu.airline.controller;

import com.seu.airline.model.Seat;
import com.seu.airline.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SeatController {

    @Autowired
    private SeatRepository seatRepository;

    // 获取航班的所有座位
    @GetMapping("/flight/{flightId}")
    public ResponseEntity<?> getFlightSeats(@PathVariable Long flightId) {
        List<Seat> seats = seatRepository.findByFlightId(flightId);
        return ResponseEntity.ok(seats);
    }

    // 获取航班的可用座位
    @GetMapping("/flight/{flightId}/available")
    public ResponseEntity<?> getAvailableSeats(@PathVariable Long flightId) {
        List<Seat> seats = seatRepository.findByFlightIdAndStatus(
                flightId,
                Seat.SeatStatus.AVAILABLE
        );
        return ResponseEntity.ok(seats);
    }

    // 获取航班特定类型的可用座位
    @GetMapping("/flight/{flightId}/type/{seatType}/available")
    public ResponseEntity<?> getAvailableSeatsByType(
            @PathVariable Long flightId,
            @PathVariable String seatType) {
        try {
            Seat.SeatType type = Seat.SeatType.valueOf(seatType.toUpperCase());
            List<Seat> seats = seatRepository.findByFlightIdAndSeatTypeAndStatus(
                    flightId,
                    type,
                    Seat.SeatStatus.AVAILABLE
            );
            return ResponseEntity.ok(seats);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("无效的座位类型");
        }
    }

    // 获取座位详情
    @GetMapping("/{id}")
    public ResponseEntity<?> getSeatById(@PathVariable Long id) {
        return seatRepository.findById(id)
                .map(seat -> ResponseEntity.ok(seat))
                .orElse(ResponseEntity.notFound().build());
    }

    // 根据航班ID和座位号获取座位
    @GetMapping("/flight/{flightId}/number/{seatNumber}")
    public ResponseEntity<?> getSeatByFlightAndNumber(
            @PathVariable Long flightId,
            @PathVariable String seatNumber) {
        return seatRepository.findByFlightIdAndSeatNumber(flightId, seatNumber)
                .map(seat -> ResponseEntity.ok(seat))
                .orElse(ResponseEntity.notFound().build());
    }
}