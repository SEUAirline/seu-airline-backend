package com.seu.airline.repository;

import com.seu.airline.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    // 根据航班ID和座位号查找
    Optional<Seat> findByFlightIdAndSeatNumber(Long flightId, String seatNumber);

    // 查找航班的所有座位
    List<Seat> findByFlightId(Long flightId);

    // 查找航班的特定类型座位
    List<Seat> findByFlightIdAndSeatType(Long flightId, Seat.SeatType seatType);

    // 查找航班的可用座位
    List<Seat> findByFlightIdAndStatus(Long flightId, Seat.SeatStatus status);

    // 查找航班特定类型的可用座位
    List<Seat> findByFlightIdAndSeatTypeAndStatus(Long flightId, Seat.SeatType seatType, Seat.SeatStatus status);
}