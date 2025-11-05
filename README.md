# SEUAirline 后端服务

基于 Spring Boot 的航空预订系统后端服务。

## 功能特性

- 用户认证（注册、登录）
- JWT 令牌认证
- 角色权限管理（用户/管理员）
- RESTful API
- 数据持久化（MySQL）
- API 文档（Swagger）
- 消息队列（RabbitMQ）用于异步处理

## 技术栈

- Spring Boot 2.7.15
- Spring Security
- Spring Data JPA
- MySQL
- JWT
- Swagger
- RabbitMQ

## 快速开始

### 环境要求

- JDK 11 或更高版本
- Maven 3.6 或更高版本
- MySQL 8.0 或更高版本
- **Redis 6.0 或更高版本** ⚠️ **必需**
- **RabbitMQ 3.8 或更高版本** ⚠️ **必需**

### 前置准备

#### 1. 启动 MySQL 服务

确保 MySQL 服务已启动，然后创建数据库：

```sql
CREATE DATABASE seu_airline CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

修改 `application.yml` 中的数据库配置（如果需要）：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/seu_airline?useSSL=false&serverTimezone=UTC&characterEncoding=utf-8
    username: root
    password: 你的密码
```

#### 2. 启动 Redis 服务 ⚠️ **必需**

本项目使用 Redis 进行缓存和会话管理，**必须先启动 Redis 服务**。

**Windows 用户：**
```bash
# 如果已安装Redis，在命令行中运行：
redis-server

# 或者双击启动 redis-server.exe
```

**Linux/Mac 用户：**
```bash
# 启动Redis服务
redis-server

# 或者使用系统服务（如果已配置）
sudo systemctl start redis
# 或
brew services start redis  # Mac
```

**验证 Redis 是否启动：**
```bash
# 打开新的终端，运行：
redis-cli ping

# 如果返回 PONG，说明Redis已启动成功
```

**Redis 配置（可选）：**

默认配置为：
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password:  # 默认无密码
    database: 0
```

如果你的 Redis 设置了密码或使用了不同的端口，请修改 `application.yml` 中的 Redis 配置。

### 构建和运行

**⚠️ 重要：启动应用前，请确保 MySQL 和 Redis 服务都已启动！**

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

### 启动顺序检查清单 ✅

在启动后端应用前，请确认：

- [ ] MySQL 服务已启动
- [ ] MySQL 数据库 `seu_airline` 已创建
- [ ] **Redis 服务已启动** ⚠️
- [ ] **RabbitMQ 服务已启动** ⚠️
- [ ] `application.yml` 中的配置正确（数据库密码、Redis地址等）

如果遇到以下错误，说明 Redis 未启动：
```
Error creating bean with name 'redisTemplate'
Unable to connect to Redis
Connection refused: localhost/127.0.0.1:6379
```

**解决方法：** 启动 Redis 服务后重新运行应用。

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

- **⚠️ 必须先启动 Redis 和 RabbitMQ 服务，否则应用无法启动**
- 首次启动时，系统会自动创建必要的数据库表
- 密码存储使用 BCrypt 加密
- JWT 令牌有效期为 24 小时（可在配置文件中修改）
- Redis 用于存储用户会话、缓存数据等
- RabbitMQ 用于处理异步任务，包括订单处理、日志收集和通知发送

## 常见问题

### Q: 启动时报错 "Unable to connect to Redis"
**A:** 请先启动 Redis 服务：
```bash
redis-server
```

### Q: Redis 如何安装？
**A:** 
- **Windows:** 下载 [Redis for Windows](https://github.com/microsoftarchive/redis/releases) 或使用 WSL
- **Mac:** `brew install redis`
- **Linux:** `sudo apt-get install redis-server` 或 `sudo yum install redis`

### Q: 如何查看 Redis 是否正在运行？
**A:** 
```bash
redis-cli ping
# 返回 PONG 表示运行正常
```

### Q: RabbitMQ 如何安装？
**A:** 
- **Windows:** (也可以通过老师群里文件安装erlang(OTP 25.3.2)和rabbitmq-server-3.12.11.exe)
  1. 下载并安装 [Erlang](https://www.erlang.org/downloads)
  2. 下载并安装 [RabbitMQ Server](https://www.rabbitmq.com/install-windows.html)
  3. 启动 RabbitMQ 服务

- **Mac:** 
  ```bash
  brew install rabbitmq
  # 启动服务
  brew services start rabbitmq
  ```

- **Linux (Ubuntu/Debian):** 
  ```bash
  # 安装 Erlang
  sudo apt-get install -y erlang
  # 安装 RabbitMQ
  sudo apt-get install -y rabbitmq-server
  # 启动服务
  sudo systemctl start rabbitmq-server
  ```

### Q: 如何查看 RabbitMQ 是否正在运行？
**A:** 
```bash
# Windows (使用命令提示符或 PowerShell)
rabbitmqctl status

# Linux/Mac
rabbitmq-diagnostics status

# 或检查 Web 管理界面是否可访问
# 默认地址: http://localhost:15672
# 默认用户名: guest
# 默认密码: guest
```

### Q: 启动时报错 "Unable to connect to RabbitMQ"
**A:** 请先启动 RabbitMQ 服务：
```bash
# Windows
net start RabbitMQ

# Linux
sudo systemctl start rabbitmq-server

# Mac
brew services start rabbitmq
```

### Q: 数据库连接失败
**A:** 检查 `application.yml` 中的数据库配置，确保用户名、密码、数据库名正确。