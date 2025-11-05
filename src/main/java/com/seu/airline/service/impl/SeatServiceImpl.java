package com.seu.airline.service.impl;

import com.seu.airline.dto.SeatDTO;
import com.seu.airline.model.Seat;
import com.seu.airline.repository.SeatRepository;
import com.seu.airline.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SeatServiceImpl implements SeatService {

    @Autowired
    private SeatRepository seatRepository;

    @Override
    public List<SeatDTO> getFlightSeats(Long flightId) {
        List<Seat> seats = seatRepository.findByFlightId(flightId);
        return seats.stream()
                .map(SeatDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeatDTO> getAvailableSeats(Long flightId) {
        List<Seat> seats = seatRepository.findByFlightIdAndStatus(
                flightId,
                Seat.SeatStatus.AVAILABLE);
        return seats.stream()
                .map(SeatDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeatDTO> getAvailableSeatsByType(Long flightId, String seatType) {
        Seat.SeatType type = Seat.SeatType.valueOf(seatType.toUpperCase());
        List<Seat> seats = seatRepository.findByFlightIdAndSeatTypeAndStatus(
                flightId,
                type,
                Seat.SeatStatus.AVAILABLE);
        return seats.stream()
                .map(SeatDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public SeatDTO getSeatById(Long seatId) {
        Optional<Seat> seat = seatRepository.findById(seatId);
        if (seat.isPresent()) {
            return SeatDTO.fromEntity(seat.get());
        }
        throw new RuntimeException("座位不存在");
    }

    @Override
    public SeatDTO getSeatByFlightAndNumber(Long flightId, String seatNumber) {
        Optional<Seat> seat = seatRepository.findByFlightIdAndSeatNumber(flightId, seatNumber);
        if (seat.isPresent()) {
            return SeatDTO.fromEntity(seat.get());
        }
        throw new RuntimeException("座位不存在");
    }
}