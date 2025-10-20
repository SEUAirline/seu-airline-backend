package com.seu.airline.repository;

import com.seu.airline.model.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {

    // 根据机场代码查找
    Optional<Airport> findByCode(String code);

    // 根据城市查找机场
    List<Airport> findByCity(String city);

    // 检查机场代码是否已存在
    boolean existsByCode(String code);
}