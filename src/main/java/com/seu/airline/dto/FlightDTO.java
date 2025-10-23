package com.seu.airline.dto;

import com.seu.airline.model.Flight;
import com.seu.airline.model.Seat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightDTO {
    private String id;
    private String flightNo;
    private String airline;
    private String departureAirport;
    private String arrivalAirport;
    private String departureCity;
    private String arrivalCity;
    private String departureTime; // HH:mm格式
    private String arrivalTime;   // HH:mm格式
    private String date;          // yyyy-MM-dd格式
    private String duration;
    private Double price;
    private Integer economySeats;
    private Integer businessSeats;
    private Integer firstClassSeats;
    private String status;
    private String aircraft;
    
    public FlightDTO(Flight flight) {
        this.id = flight.getId() != null ? flight.getId().toString() : null;
        this.flightNo = flight.getFlightNumber();
        this.airline = flight.getAirline() != null ? flight.getAirline().getName() : "";
        this.departureAirport = flight.getDepartureAirport() != null ? flight.getDepartureAirport().getCode() : "";
        this.arrivalAirport = flight.getArrivalAirport() != null ? flight.getArrivalAirport().getCode() : "";
        this.departureCity = flight.getDepartureAirport() != null ? flight.getDepartureAirport().getCity() : "";
        this.arrivalCity = flight.getArrivalAirport() != null ? flight.getArrivalAirport().getCity() : "";
        
        // 格式化时间
        if (flight.getDepartureTime() != null) {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            this.departureTime = flight.getDepartureTime().format(timeFormatter);
            this.date = flight.getDepartureTime().format(dateFormatter);
        }
        
        if (flight.getArrivalTime() != null) {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            this.arrivalTime = flight.getArrivalTime().format(timeFormatter);
        }
        
        // 计算飞行时长
        if (flight.getDepartureTime() != null && flight.getArrivalTime() != null) {
            this.duration = calculateDuration(flight.getDepartureTime(), flight.getArrivalTime());
        }
        
        this.status = flight.getStatus() != null ? flight.getStatus().name().toLowerCase() : "scheduled";
        this.aircraft = flight.getAircraftType();
        
        // 统计座位数量和价格
        if (flight.getSeats() != null && !flight.getSeats().isEmpty()) {
            calculateSeatInfo(flight.getSeats());
        } else {
            this.economySeats = 0;
            this.businessSeats = 0;
            this.firstClassSeats = 0;
            this.price = 0.0;
        }
    }
    
    private void calculateSeatInfo(List<Seat> seats) {
        int economy = 0;
        int business = 0;
        int first = 0;
        BigDecimal minPrice = null;
        
        for (Seat seat : seats) {
            // 统计可用座位数
            if (seat.getStatus() == Seat.SeatStatus.AVAILABLE) {
                switch (seat.getSeatType()) {
                    case ECONOMY:
                        economy++;
                        break;
                    case BUSINESS:
                        business++;
                        break;
                    case FIRST:
                        first++;
                        break;
                }
                
                // 找到最低价格
                if (minPrice == null || seat.getPrice().compareTo(minPrice) < 0) {
                    minPrice = seat.getPrice();
                }
            }
        }
        
        this.economySeats = economy;
        this.businessSeats = business;
        this.firstClassSeats = first;
        this.price = minPrice != null ? minPrice.doubleValue() : 0.0;
    }
    
    private String calculateDuration(LocalDateTime departure, LocalDateTime arrival) {
        long minutes = java.time.Duration.between(departure, arrival).toMinutes();
        long hours = minutes / 60;
        long mins = minutes % 60;
        return String.format("%dh %dm", hours, mins);
    }
}
