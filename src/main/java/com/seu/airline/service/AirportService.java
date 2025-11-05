package com.seu.airline.service;

import com.seu.airline.dto.AirportDTO;

import java.util.List;

public interface AirportService {
    
    /**
     * 获取所有机场列表
     * @return 机场列表
     */
    List<AirportDTO> getAllAirports();
    
    /**
     * 根据代码获取机场
     * @param code 机场代码
     * @return 机场对象
     */
    AirportDTO getAirportByCode(String code);
}