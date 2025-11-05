package com.seu.airline.service;

import com.seu.airline.dto.SeatDTO;

import java.util.List;

public interface SeatService {
    
    /**
     * 获取航班的所有座位
     * @param flightId 航班ID
     * @return 座位列表
     */
    List<SeatDTO> getFlightSeats(Long flightId);
    
    /**
     * 获取航班的可用座位
     * @param flightId 航班ID
     * @return 可用座位列表
     */
    List<SeatDTO> getAvailableSeats(Long flightId);
    
    /**
     * 获取航班特定类型的可用座位
     * @param flightId 航班ID
     * @param seatType 座位类型
     * @return 特定类型可用座位列表
     */
    List<SeatDTO> getAvailableSeatsByType(Long flightId, String seatType);
    
    /**
     * 获取座位详情
     * @param seatId 座位ID
     * @return 座位详情
     */
    SeatDTO getSeatById(Long seatId);
    
    /**
     * 根据航班ID和座位号获取座位
     * @param flightId 航班ID
     * @param seatNumber 座位号
     * @return 座位详情
     */
    SeatDTO getSeatByFlightAndNumber(Long flightId, String seatNumber);
}