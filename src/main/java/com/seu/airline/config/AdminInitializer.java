package com.seu.airline.config;

import com.seu.airline.model.User;
import com.seu.airline.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已存在管理员账户
        if (!userRepository.existsByUsername("admin")) {
            // 创建默认管理员账户
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@seuairline.com");
            admin.setFullName("系统管理员");
            admin.setPhone("13800138000");
            admin.setRole(User.Role.ADMIN); // 管理员角色
            admin.setStatus(1); // 启用状态
            userRepository.save(admin);
            System.out.println("默认管理员账户已创建: username=admin, password=admin123");
        }
        
        // 创建默认乘客账户用于测试
        if (!userRepository.existsByUsername("passenger1")) {
            User passenger = new User();
            passenger.setUsername("passenger1");
            passenger.setPassword(passwordEncoder.encode("passenger123"));
            passenger.setEmail("passenger1@example.com");
            passenger.setFullName("测试乘客");
            passenger.setPhone("13900139000");
            passenger.setRole(User.Role.PASSENGER); // 乘客角色
            passenger.setStatus(1); // 启用状态
            userRepository.save(passenger);
            System.out.println("默认乘客账户已创建: username=passenger1, password=passenger123");
        }
    }
}