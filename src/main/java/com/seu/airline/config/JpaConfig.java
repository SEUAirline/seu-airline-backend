package com.seu.airline.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 审计配置
 * 启用 @CreatedDate 和 @LastModifiedDate 注解自动填充时间戳
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // 无需额外配置，注解即可生效
}
