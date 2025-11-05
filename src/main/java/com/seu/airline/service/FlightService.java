package com.seu.airline.service;

import com.seu.airline.dto.FlightDTO;
import com.seu.airline.model.Flight;

import java.util.List;

public interface FlightService {
    
    /**
     * 搜索航班
     * @param departureCity 出发城市
     * @param arrivalCity 到达城市
     * @param departureDate 出发日期
     * @return 航班列表
     */
    List<FlightDTO> searchFlights(String departureCity, String arrivalCity, String departureDate);
    
    /**
     * 根据ID获取航班详情
     * @param flightId 航班ID
     * @return 航班对象
     */
    FlightDTO getFlightById(Long flightId);
    
    /**
     * 获取即将起飞的航班
     * @return 航班列表
     */
    List<Flight> getUpcomingFlights();
    
    /**
     * 根据航班号查询
     * @param flightNumber 航班号
     * @return 航班对象
     */
    Flight getFlightByNumber(String flightNumber);
}