# SEUAirline 后端服务

基于 Spring Boot 的航空预订系统后端服务。

## 功能特性

- 用户认证（注册、登录）
- JWT 令牌认证
- 角色权限管理（用户/管理员）
- RESTful API
- 数据持久化（MySQL）
- API 文档（Swagger）

## 技术栈

- Spring Boot 2.7.15
- Spring Security
- Spring Data JPA
- MySQL
- JWT
- Swagger

## 快速开始

### 环境要求

- JDK 11 或更高版本
- Maven 3.6 或更高版本
- MySQL 8.0 或更高版本

### 数据库配置

1. 确保 MySQL 服务已启动
2. 创建数据库：
```sql
CREATE DATABASE seu_airline CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
3. 修改 `application.yml` 中的数据库配置（如果需要）：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/seu_airline?useSSL=false&serverTimezone=UTC&characterEncoding=utf-8
    username: root
    password: 123456
```

### 构建和运行

1. 构建项目：
```bash
mvn clean package
```

2. 运行应用：
```bash
java -jar target/seu-airline-backend-1.0.0.jar
```

或者直接运行：
```bash
mvn spring-boot:run
```

## API 端点

### 认证接口

- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/me` - 获取当前用户信息（需要认证）

### 管理员默认账户

系统启动时会自动创建默认管理员账户：
- 用户名：`admin`
- 密码：`admin123`

## API 文档

启动应用后，可以通过以下地址访问 Swagger API 文档：
```
http://localhost:8080/api/swagger-ui/
```

## 注意事项

- 首次启动时，系统会自动创建必要的数据库表
- 密码存储使用 BCrypt 加密
- JWT 令牌有效期为 24 小时（可在配置文件中修改）