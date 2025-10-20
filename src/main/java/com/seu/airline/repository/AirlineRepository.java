package com.seu.airline.repository;

import com.seu.airline.model.Airline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AirlineRepository extends JpaRepository<Airline, Long> {

    // 根据航空公司代码查找
    Optional<Airline> findByCode(String code);

    // 检查航空公司代码是否已存在
    boolean existsByCode(String code);
}