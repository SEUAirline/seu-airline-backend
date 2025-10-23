package com.seu.airline.dto;

import com.seu.airline.model.Airport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirportDTO {
    private String code;
    private String name;
    private String city;

    public AirportDTO(Airport airport) {
        this.code = airport.getCode();
        this.name = airport.getName();
        this.city = airport.getCity();
    }
}
