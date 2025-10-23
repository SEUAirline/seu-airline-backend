package com.seu.airline.controller;

import com.seu.airline.dto.ApiResponse;
import com.seu.airline.dto.FlightDTO;
import com.seu.airline.model.Flight;
import com.seu.airline.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/flight")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FlightController {

    @Autowired
    private FlightRepository flightRepository;

    // 搜索航班（按城市、日期）
    @GetMapping("/search")
    public ResponseEntity<?> searchFlights(
            @RequestParam String departureCity,
            @RequestParam String arrivalCity,
            @RequestParam String departureDate) {
        try {
            LocalDate date = LocalDate.parse(departureDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDateTime startDate = date.atStartOfDay();
            LocalDateTime endDate = startDate.plusDays(1);

            List<Flight> flights = flightRepository.findFlightsByCity(
                    departureCity,
                    arrivalCity,
                    startDate,
                    endDate);

            List<FlightDTO> flightDTOs = flights.stream()
                    .map(FlightDTO::new)
                    .collect(Collectors.toList());

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
            Optional<Flight> flight = flightRepository.findById(flightId);
            if (flight.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(new FlightDTO(flight.get())));
            } else {
                return ResponseEntity.status(404).body(ApiResponse.error("航班不存在"));
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("无效的航班ID"));
        }
    }

    // 获取即将起飞的航班（保留）
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingFlights() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);

        List<Flight> flights = flightRepository.findUpcomingFlights(now, tomorrow);
        return ResponseEntity.ok(ApiResponse.success(flights));
    }

    // 根据航班号查询（保留）
    @GetMapping("/number/{flightNumber}")
    public ResponseEntity<?> getFlightByNumber(@PathVariable String flightNumber) {
        Optional<Flight> flight = flightRepository.findByFlightNumber(flightNumber);
        if (flight.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(flight.get()));
        } else {
            return ResponseEntity.status(404).body(ApiResponse.error("航班不存在"));
        }
    }
}