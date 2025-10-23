package com.seu.airline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@SpringBootApplication
public class SeuAirlineBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeuAirlineBackendApplication.class, args);
    }

    // 配置CORS过滤器
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // 使用 addAllowedOriginPattern 替代 addAllowedOrigin 以支持 allowCredentials
        config.addAllowedOriginPattern("*"); // 开发环境允许所有源
        // 生产环境建议指定具体前端地址，例如：
        // config.addAllowedOrigin("http://localhost:5173");
        // config.addAllowedOrigin("https://yourdomain.com");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}