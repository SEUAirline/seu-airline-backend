package com.seu.airline.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "flights")
@Data
@NoArgsConstructor
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_number", nullable = false, unique = true, length = 20)
    private String flightNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airline_id", nullable = false)
    @JsonIgnoreProperties("flights")
    private Airline airline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_airport_id", nullable = false)
    @JsonIgnoreProperties({"departingFlights", "arrivingFlights"})
    private Airport departureAirport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arrival_airport_id", nullable = false)
    @JsonIgnoreProperties({"departingFlights", "arrivingFlights"})
    private Airport arrivalAirport;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "aircraft_type", length = 50)
    private String aircraftType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FlightStatus status = FlightStatus.SCHEDULED;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnoreProperties("flight")
    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seat> seats;

    // 航班状态枚举
    public enum FlightStatus {
        SCHEDULED, DELAYED, CANCELLED, DEPARTED, ARRIVED
    }
}