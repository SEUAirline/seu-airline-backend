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
@Table(name = "airlines")
@Data
@NoArgsConstructor
public class Airline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnoreProperties("airline")
    @OneToMany(mappedBy = "airline", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Flight> flights;
}