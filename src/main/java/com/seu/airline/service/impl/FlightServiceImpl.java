package com.seu.airline.service.impl;

import com.seu.airline.dto.FlightDTO;
import com.seu.airline.model.Flight;
import com.seu.airline.repository.FlightRepository;
import com.seu.airline.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FlightServiceImpl implements FlightService {

    @Autowired
    private FlightRepository flightRepository;

    @Override
    public List<FlightDTO> searchFlights(String departureCity, String arrivalCity, String departureDate) {
        LocalDate date = LocalDate.parse(departureDate, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = startDate.plusDays(1);

        List<Flight> flights = flightRepository.findFlightsByCity(
                departureCity,
                arrivalCity,
                startDate,
                endDate);

        return flights.stream()
                .map(FlightDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public FlightDTO getFlightById(Long flightId) {
        Optional<Flight> flight = flightRepository.findById(flightId);
        if (flight.isPresent()) {
            return new FlightDTO(flight.get());
        }
        throw new RuntimeException("航班不存在");
    }

    @Override
    public List<Flight> getUpcomingFlights() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        return flightRepository.findUpcomingFlights(now, tomorrow);
    }

    @Override
    public Flight getFlightByNumber(String flightNumber) {
        Optional<Flight> flight = flightRepository.findByFlightNumber(flightNumber);
        if (flight.isPresent()) {
            return flight.get();
        }
        throw new RuntimeException("航班不存在");
    }
}