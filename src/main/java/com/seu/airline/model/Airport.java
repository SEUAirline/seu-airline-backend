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
@Table(name = "airports")
@Data
@NoArgsConstructor
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @Column(name = "country", nullable = false, length = 50)
    private String country;

    @Column(name = "timezone", length = 50)
    private String timezone;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnoreProperties({"departureAirport", "arrivalAirport"})
    @OneToMany(mappedBy = "departureAirport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Flight> departingFlights;

    @JsonIgnoreProperties({"departureAirport", "arrivalAirport"})
    @OneToMany(mappedBy = "arrivalAirport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Flight> arrivingFlights;
}