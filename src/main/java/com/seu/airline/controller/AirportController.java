package com.seu.airline.controller;

import com.seu.airline.dto.ApiResponse;
import com.seu.airline.service.AirportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/airport")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AirportController {

    @Autowired
    private AirportService airportService;

    // 获取所有机场列表
    @GetMapping("/list")
    public ResponseEntity<?> getAllAirports() {
        try {
            var airportDTOs = airportService.getAllAirports();
            return ResponseEntity.ok(ApiResponse.success(airportDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取机场列表失败"));
        }
    }

    // 根据代码获取机场
    @GetMapping("/{code}")
    public ResponseEntity<?> getAirportByCode(@PathVariable String code) {
        try {
            var airportDTO = airportService.getAirportByCode(code);
            return ResponseEntity.ok(ApiResponse.success(airportDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }
}
