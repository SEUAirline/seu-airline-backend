package com.seu.airline.repository;

import com.seu.airline.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 根据用户名查找用户
    Optional<User> findByUsername(String username);

    // 根据邮箱查找用户
    Optional<User> findByEmail(String email);

    // 根据角色查找用户
    List<User> findByRole(User.Role role);

    // 根据状态查找用户
    List<User> findByStatus(Integer status);

    // 检查用户名是否存在
    boolean existsByUsername(String username);

    // 检查邮箱是否存在
    boolean existsByEmail(String email);
}