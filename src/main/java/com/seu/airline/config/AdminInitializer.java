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
            admin.setRole(1); // 管理员角色
            userRepository.save(admin);
            System.out.println("默认管理员账户已创建: username=admin, password=admin123");
        }
    }
}