package com.seu.airline.controller;

import com.seu.airline.dto.ApiResponse;
import com.seu.airline.service.FlightService;
import com.seu.airline.service.FlightViewHistoryService;
import com.seu.airline.service.FlightRecommenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flight")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FlightController {

    @Autowired
    private FlightService flightService;
    
    @Autowired
    private FlightViewHistoryService flightViewHistoryService;

    @Autowired
    private FlightRecommenderService flightRecommenderService;

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
            
            // 记录用户查看历史（如果用户已登录）
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                    Long userId = Long.parseLong(userDetails.getUsername());
                    flightViewHistoryService.recordViewHistory(userId, flightId);
                }
            } catch (Exception e) {
                // 如果记录历史失败，不影响航班详情的返回
                // 可以在这里添加日志记录
            }
            
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
            
            // 记录用户查看历史（如果用户已登录）
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                    Long userId = Long.parseLong(userDetails.getUsername());
                    flightViewHistoryService.recordViewHistory(userId, flight.getId());
                }
            } catch (Exception e) {
                // 如果记录历史失败，不影响航班详情的返回
            }
            
            return ResponseEntity.ok(ApiResponse.success(flight));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // 获取推荐航班
    @GetMapping("/recommended")
    public ResponseEntity<?> getRecommendedFlights(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                Long userId = Long.parseLong(userDetails.getUsername());
                
                // 限制最大返回数量
                int actualLimit = Math.min(limit, 20);
                var recommendedFlights = flightViewHistoryService.getRecommendedFlights(userId, actualLimit);
                return ResponseEntity.ok(ApiResponse.success(recommendedFlights));
            }
            
            // 如果用户未登录，返回即将起飞的航班作为默认推荐
            var upcomingFlights = flightService.getUpcomingFlights().stream()
                    .limit(Math.min(limit, 20))
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(upcomingFlights));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取推荐航班失败"));
        }
    }
    
    // 获取用户查看历史（可选）
    @GetMapping("/history")
    public ResponseEntity<?> getViewHistory(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                Long userId = Long.parseLong(userDetails.getUsername());
                
                // 限制最大返回数量
                int actualLimit = Math.min(limit, 50);
                var history = flightViewHistoryService.getViewHistory(userId, actualLimit);
                return ResponseEntity.ok(ApiResponse.success(history));
            }
            
            return ResponseEntity.status(401).body(ApiResponse.error("用户未登录"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取查看历史失败"));
        }
    }
    
    // 获取基于神经网络的个性化推荐航班
    @GetMapping("/personalized-recommendations")
    public ResponseEntity<?> getPersonalizedRecommendations(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                Long userId = Long.parseLong(userDetails.getUsername());
                
                // 限制最大返回数量
                int actualLimit = Math.min(limit, 20);
                var recommendations = flightRecommenderService.getPersonalizedRecommendations(userId, actualLimit);
                return ResponseEntity.ok(ApiResponse.success(recommendations));
            }
            
            // 如果用户未登录，返回即将起飞的航班作为默认推荐
            var upcomingFlights = flightService.getUpcomingFlights().stream()
                    .limit(Math.min(limit, 20))
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(upcomingFlights));
        } catch (Exception e) {
            // 如果神经网络推荐失败，回退到基于规则的推荐
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                    Long userId = Long.parseLong(userDetails.getUsername());
                    
                    int actualLimit = Math.min(limit, 20);
                    var fallbackRecommendations = flightViewHistoryService.getRecommendedFlights(userId, actualLimit);
                    return ResponseEntity.ok(ApiResponse.success(fallbackRecommendations));
                }
            } catch (Exception fallbackException) {
                // 忽略回退逻辑中的异常
            }
            
            return ResponseEntity.badRequest().body(ApiResponse.error("获取个性化推荐失败"));
        }
    }
}