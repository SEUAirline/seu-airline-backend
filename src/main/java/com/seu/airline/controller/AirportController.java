package com.seu.airline.controller;

import com.seu.airline.dto.AirportDTO;
import com.seu.airline.dto.ApiResponse;
import com.seu.airline.model.Airport;
import com.seu.airline.repository.AirportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/airport")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AirportController {

    @Autowired
    private AirportRepository airportRepository;

    // 获取所有机场列表
    @GetMapping("/list")
    public ResponseEntity<?> getAllAirports() {
        List<Airport> airports = airportRepository.findAll();
        List<AirportDTO> airportDTOs = airports.stream()
                .map(AirportDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(airportDTOs));
    }

    // 根据代码获取机场
    @GetMapping("/{code}")
    public ResponseEntity<?> getAirportByCode(@PathVariable String code) {
        return airportRepository.findByCode(code)
                .map(airport -> ResponseEntity.ok(ApiResponse.success(new AirportDTO(airport))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("机场不存在")));
    }
}
