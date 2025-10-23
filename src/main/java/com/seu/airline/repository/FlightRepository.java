package com.seu.airline.repository;

import com.seu.airline.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    // 根据航班号查找
    Optional<Flight> findByFlightNumber(String flightNumber);

    // 根据出发和到达机场以及日期范围查询航班
    @Query("SELECT f FROM Flight f WHERE f.departureAirport.id = :departureAirportId AND f.arrivalAirport.id = :arrivalAirportId AND f.departureTime BETWEEN :startDate AND :endDate AND f.status <> 'CANCELLED'")
    List<Flight> findFlights(@Param("departureAirportId") Long departureAirportId,
            @Param("arrivalAirportId") Long arrivalAirportId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // 根据城市查询航班
    @Query("SELECT f FROM Flight f WHERE f.departureAirport.city = :departureCity AND f.arrivalAirport.city = :arrivalCity AND f.departureTime BETWEEN :startDate AND :endDate AND f.status <> 'CANCELLED'")
    List<Flight> findFlightsByCity(@Param("departureCity") String departureCity,
            @Param("arrivalCity") String arrivalCity,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // 根据航空公司查询航班
    List<Flight> findByAirlineId(Long airlineId);

    // 查询特定状态的航班
    List<Flight> findByStatus(Flight.FlightStatus status);

    // 查询即将起飞的航班（未来24小时内）
    @Query("SELECT f FROM Flight f WHERE f.departureTime BETWEEN :now AND :tomorrow AND f.status = 'SCHEDULED'")
    List<Flight> findUpcomingFlights(@Param("now") LocalDateTime now, @Param("tomorrow") LocalDateTime tomorrow);
}