package com.seu.airline.dto;

import com.seu.airline.model.Seat;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 座位数据传输对象
 */
@Data
public class SeatDTO {
    private Long id;
    private Long flightId;
    private String seatNumber;
    private String seatType;
    private BigDecimal price;
    private String status;

    /**
     * 从 Seat 实体创建 DTO
     */
    public static SeatDTO fromEntity(Seat seat) {
        SeatDTO dto = new SeatDTO();
        dto.setId(seat.getId());
        dto.setFlightId(seat.getFlight().getId());
        dto.setSeatNumber(seat.getSeatNumber());
        dto.setSeatType(seat.getSeatType().name());
        dto.setPrice(seat.getPrice());
        dto.setStatus(seat.getStatus().name());
        return dto;
    }
}
