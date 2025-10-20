package com.seu.airline.controller;

import com.seu.airline.model.Flight;
import com.seu.airline.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/flights")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FlightController {

    @Autowired
    private FlightRepository flightRepository;

    // 搜索航班
    @GetMapping("/search")
    public ResponseEntity<?> searchFlights(
            @RequestParam Long departureAirportId,
            @RequestParam Long arrivalAirportId,
            @RequestParam String date) {
        try {
            // 解析日期
            LocalDateTime startDate = LocalDateTime.parse(date + "T00:00:00");
            LocalDateTime endDate = startDate.plusDays(1);

            List<Flight> flights = flightRepository.findFlights(
                    departureAirportId,
                    arrivalAirportId,
                    startDate,
                    endDate
            );

            return ResponseEntity.ok(flights);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("日期格式错误，请使用YYYY-MM-DD格式");
        }
    }

    // 获取航班详情
    @GetMapping("/{id}")
    public ResponseEntity<?> getFlightById(@PathVariable Long id) {
        Optional<Flight> flight = flightRepository.findById(id);
        if (flight.isPresent()) {
            return ResponseEntity.ok(flight.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 获取即将起飞的航班
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingFlights() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        
        List<Flight> flights = flightRepository.findUpcomingFlights(now, tomorrow);
        return ResponseEntity.ok(flights);
    }

    // 根据航班号查询
    @GetMapping("/number/{flightNumber}")
    public ResponseEntity<?> getFlightByNumber(@PathVariable String flightNumber) {
        Optional<Flight> flight = flightRepository.findByFlightNumber(flightNumber);
        if (flight.isPresent()) {
            return ResponseEntity.ok(flight.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}