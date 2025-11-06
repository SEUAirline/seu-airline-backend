package com.seu.airline.service;

import com.seu.airline.dto.FlightDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FlightViewHistoryService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private FlightService flightService;

    // 每个用户最多记录的查看历史数量
    private static final int MAX_HISTORY_SIZE = 20;
    // 查看历史的过期时间（30天）
    private static final long HISTORY_EXPIRE_DAYS = 30;

    /**
     * 记录用户查看航班的历史
     * @param userId 用户ID
     * @param flightId 航班ID
     */
    public void recordViewHistory(Long userId, Long flightId) {
        String key = "user:history:" + userId;
        
        // 使用Redis的列表结构，最新的放在最前面
        redisTemplate.opsForList().leftPush(key, flightId.toString());
        
        // 移除重复的记录（保留最新的）
        Set<Object> existingIds = new HashSet<>();
        List<Object> allItems = redisTemplate.opsForList().range(key, 0, -1);
        if (allItems != null) {
            for (Object item : allItems) {
                if (!existingIds.add(item)) {
                    redisTemplate.opsForList().remove(key, 1, item);
                }
            }
        }
        
        // 限制历史记录数量
        redisTemplate.opsForList().trim(key, 0, MAX_HISTORY_SIZE - 1);
        
        // 设置过期时间
        redisTemplate.expire(key, HISTORY_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 获取用户的航班查看历史
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 航班历史记录列表
     */
    public List<FlightDTO> getViewHistory(Long userId, int limit) {
        String key = "user:history:" + userId;
        List<Object> flightIds = redisTemplate.opsForList().range(key, 0, limit - 1);
        
        if (flightIds == null || flightIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return flightIds.stream()
                .map(id -> {
                    try {
                        Long flightId = Long.parseLong(id.toString());
                        return flightService.getFlightById(flightId);
                    } catch (Exception e) {
                        // 如果航班不存在，从历史记录中移除
                        redisTemplate.opsForList().remove(key, 1, id);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取推荐航班（基于用户的查看历史）
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 推荐航班列表
     */
    public List<FlightDTO> getRecommendedFlights(Long userId, int limit) {
        // 获取用户最近查看的航班历史
        List<FlightDTO> recentViews = getViewHistory(userId, 5);
        
        if (recentViews.isEmpty()) {
            // 如果没有历史记录，返回即将起飞的航班
            return flightService.getUpcomingFlights().stream()
                    .map(FlightDTO::new)
                    .limit(limit)
                    .collect(Collectors.toList());
        }
        
        // 提取用户感兴趣的航线特征（出发城市、到达城市等）
        Map<String, Integer> cityPopularity = new HashMap<>();
        for (FlightDTO flight : recentViews) {
            // 增加出发城市和到达城市的权重
            cityPopularity.put(flight.getDepartureCity(), 
                cityPopularity.getOrDefault(flight.getDepartureCity(), 0) + 2);
            cityPopularity.put(flight.getArrivalCity(), 
                cityPopularity.getOrDefault(flight.getArrivalCity(), 0) + 2);
        }
        
        // 获取最热门的城市
        List<String> popularCities = cityPopularity.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // 基于热门城市查找推荐航班
        Set<FlightDTO> recommendedFlights = new HashSet<>();
        for (String city : popularCities) {
            // 查找出发城市为热门城市的航班
            List<FlightDTO> departureFlights = flightService.getUpcomingFlights().stream()
                    .filter(f -> f.getDepartureAirport() != null && f.getDepartureAirport().getCity().equals(city))
                    .map(FlightDTO::new)
                    .collect(Collectors.toList());
            recommendedFlights.addAll(departureFlights);
            
            // 查找到达城市为热门城市的航班
            List<FlightDTO> arrivalFlights = flightService.getUpcomingFlights().stream()
                    .filter(f -> f.getArrivalAirport() != null && f.getArrivalAirport().getCity().equals(city))
                    .map(FlightDTO::new)
                    .collect(Collectors.toList());
            recommendedFlights.addAll(arrivalFlights);
        }
        
        // 移除用户最近已经查看过的航班
        Set<String> recentViewedIds = recentViews.stream()
                .map(FlightDTO::getId)
                .collect(Collectors.toSet());
        
        return recommendedFlights.stream()
                .filter(f -> !recentViewedIds.contains(f.getId()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}