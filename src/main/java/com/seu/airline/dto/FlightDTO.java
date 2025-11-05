package com.seu.airline.dto;

import com.seu.airline.model.Flight;
import com.seu.airline.model.Seat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private String departureTime;   // ISO-8601 + 时区偏移
    private String arrivalTime;
    private String date;            // yyyy-MM-dd
    private String duration;
    private Double price;           // 最低票价
    private Integer economySeats;
    private Integer businessSeats;
    private Integer firstClassSeats;
    private String status;
    private String aircraft;

    // 推荐固定为中国时区，避免服务器是 UTC 导致偏差
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter ISO_OFFSET = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public FlightDTO(Flight flight) {
        if (flight == null) return;

        this.id = flight.getId() != null ? flight.getId().toString() : null;
        this.flightNo = flight.getFlightNumber();

        // 航空公司
        this.airline = flight.getAirline() != null ? flight.getAirline().getName() : "";

        // 机场 + 城市
        if (flight.getDepartureAirport() != null) {
            this.departureAirport = flight.getDepartureAirport().getCode();
            this.departureCity = flight.getDepartureAirport().getCity();
        }
        if (flight.getArrivalAirport() != null) {
            this.arrivalAirport = flight.getArrivalAirport().getCode();
            this.arrivalCity = flight.getArrivalAirport().getCity();
        }

        // 时间字段（格式化为 ISO-8601 + 时区偏移，便于前端 new Date() 直接使用）
        if (flight.getDepartureTime() != null) {
            LocalDateTime dep = flight.getDepartureTime();
            this.date = dep.format(DATE_FORMAT);
            this.departureTime = dep.atZone(DEFAULT_ZONE).format(ISO_OFFSET);
        }

        if (flight.getArrivalTime() != null) {
            LocalDateTime arr = flight.getArrivalTime();
            this.arrivalTime = arr.atZone(DEFAULT_ZONE).format(ISO_OFFSET);
        }

        if (flight.getDepartureTime() != null && flight.getArrivalTime() != null) {
            this.duration = calculateDuration(flight.getDepartureTime(), flight.getArrivalTime());
        }

        this.status = flight.getStatus() != null ? flight.getStatus().name().toLowerCase() : "scheduled";
        this.aircraft = flight.getAircraftType();

        // 处理座位 + 最低价格
        if (flight.getSeats() != null && !flight.getSeats().isEmpty()) {
            calculateSeatInfo(flight.getSeats());
        } else {
            this.economySeats = 0;
            this.businessSeats = 0;
            this.firstClassSeats = 0;
            this.price = 0.0;
        }
    }

    /**
     * 计算可用座位和最低价格（包含全部座位，只要价格不是 null）
     */
    private void calculateSeatInfo(List<Seat> seats) {
        int economy = 0, business = 0, first = 0;
        BigDecimal minPrice = null;

        for (Seat seat : seats) {
            if (seat == null) continue;

            // ✅ 仅统计可售座位
            if (seat.getStatus() == Seat.SeatStatus.AVAILABLE && seat.getSeatType() != null) {
                switch (seat.getSeatType()) {
                    case ECONOMY -> economy++;
                    case BUSINESS -> business++;
                    case FIRST -> first++;
                }
            }

            // ✅ 价格不为 null 时参与最低价比对（无论是否已售）
            BigDecimal price = seat.getPrice();
            if (price != null && (minPrice == null || price.compareTo(minPrice) < 0)) {
                minPrice = price;
            }
        }

        this.economySeats = economy;
        this.businessSeats = business;
        this.firstClassSeats = first;
        this.price = minPrice != null ? minPrice.doubleValue() : 0.0;
    }

    /**
     * 计算飞行时长
     */
    private String calculateDuration(LocalDateTime departure, LocalDateTime arrival) {
        long minutes = Duration.between(departure, arrival).toMinutes();
        return String.format("%dh %dm", minutes / 60, minutes % 60);
    }
}
