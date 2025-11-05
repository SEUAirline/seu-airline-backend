package com.seu.airline.controller;

import com.seu.airline.dto.ApiResponse;
import com.seu.airline.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flight")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FlightController {

    @Autowired
    private FlightService flightService;

    // 搜索航班（按城市、日期）
    @GetMapping("/search")
    public ResponseEntity<?> searchFlights(
            @RequestParam String departureCity,
            @RequestParam String arrivalCity,
            @RequestParam String departureDate) {
        try {
            var flightDTOs = flightService.searchFlights(departureCity, arrivalCity, departureDate);
            return ResponseEntity.ok(ApiResponse.success(flightDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("日期格式错误，请使用YYYY-MM-DD格式"));
        }
    }

    // 获取航班详情
    @GetMapping("/{id}")
    public ResponseEntity<?> getFlightById(@PathVariable String id) {
        try {
            Long flightId = Long.parseLong(id);
            var flightDTO = flightService.getFlightById(flightId);
            return ResponseEntity.ok(ApiResponse.success(flightDTO));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("无效的航班ID"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    // 获取即将起飞的航班（保留）
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingFlights() {
        try {
            var flights = flightService.getUpcomingFlights();
            return ResponseEntity.ok(ApiResponse.success(flights));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取航班失败"));
        }
    }

    // 根据航班号查询（保留）
    @GetMapping("/number/{flightNumber}")
    public ResponseEntity<?> getFlightByNumber(@PathVariable String flightNumber) {
        try {
            var flight = flightService.getFlightByNumber(flightNumber);
            return ResponseEntity.ok(ApiResponse.success(flight));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }
}