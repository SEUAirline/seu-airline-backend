package com.seu.airline.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;
import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.OAS_30)  // 使用OpenAPI 3.0
                .apiInfo(apiInfo())
                .securityContexts(Collections.singletonList(securityContext()))
                .securitySchemes(Collections.singletonList(apiKey()))
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.seu.airline.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    // 构建API文档信息
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("SEUAirline API")
                .description("航空预订系统后端API文档")
                .version("1.0.0")
                .contact(new Contact("SEU", "http://seu.edu.cn", ""))
                .build();
    }

    // 配置JWT认证
    private ApiKey apiKey() {
        return new ApiKey("JWT", "Authorization", "header");
    }

    // 配置安全上下文
    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex("^(?!/auth/).*$")).build();
    }

    // 默认认证引用
    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Collections.singletonList(new SecurityReference("JWT", authorizationScopes));
    }
}