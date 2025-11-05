package com.seu.airline.service.impl;

import com.seu.airline.dto.AirportDTO;
import com.seu.airline.model.Airport;
import com.seu.airline.repository.AirportRepository;
import com.seu.airline.service.AirportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AirportServiceImpl implements AirportService {

    @Autowired
    private AirportRepository airportRepository;

    @Override
    public List<AirportDTO> getAllAirports() {
        List<Airport> airports = airportRepository.findAll();
        return airports.stream()
                .map(AirportDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public AirportDTO getAirportByCode(String code) {
        Optional<Airport> airport = airportRepository.findByCode(code);
        if (airport.isPresent()) {
            return new AirportDTO(airport.get());
        }
        throw new RuntimeException("机场不存在");
    }
}