# SEUAirline 后端快速启动指南

## 🚀 启动前检查清单

在启动后端应用之前，请按顺序完成以下步骤：

### ✅ 第一步：安装必需软件

确保已安装以下软件：

- [ ] JDK 11 或更高版本
- [ ] Maven 3.6 或更高版本  
- [ ] MySQL 8.0 或更高版本
- [ ] **Redis 6.0 或更高版本** ⚠️ **必需！**

### ✅ 第二步：启动 MySQL

1. 启动 MySQL 服务

2. 创建数据库：
   ```sql
   CREATE DATABASE seu_airline CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. 修改 `application.yml` 中的数据库配置：
   ```yaml
   spring:
     datasource:
       username: root
       password: 你的MySQL密码
   ```

### ✅ 第三步：启动 Redis ⚠️ **重要！**

**Windows 用户：**
```bash
# 在命令行中运行
redis-server

# 或双击 redis-server.exe
```

**Linux/Mac 用户：**
```bash
redis-server

# 或使用系统服务
sudo systemctl start redis  # Linux
brew services start redis   # Mac
```

**验证 Redis 是否启动：**
```bash
redis-cli ping
# 应该返回：PONG
```

### ✅ 第四步：启动后端应用

```bash
# 方式1：使用Maven
cd seu-airline-backend
mvn spring-boot:run

# 方式2：打包后运行
mvn clean package
java -jar target/seu-airline-backend-1.0.0.jar
```

### ✅ 第五步：验证启动成功

1. 检查控制台输出，应该看到：
   ```
   Started SeuAirlineBackendApplication in xxx seconds
   ```

2. 访问 Swagger API 文档：
   ```
   http://localhost:8080/api/swagger-ui/
   ```

3. 测试健康检查：
   ```
   http://localhost:8080/api/health
   ```

---

## ⚠️ 常见启动错误

### 错误1：Redis 连接失败

**错误信息：**
```
Error creating bean with name 'redisTemplate'
Unable to connect to Redis
Connection refused: localhost/127.0.0.1:6379
```

**解决方法：**
```bash
# 启动 Redis 服务
redis-server

# 验证 Redis 是否运行
redis-cli ping
```

### 错误2：MySQL 连接失败

**错误信息：**
```
Access denied for user 'root'@'localhost'
Unknown database 'seu_airline'
```

**解决方法：**
1. 检查 MySQL 服务是否启动
2. 检查 `application.yml` 中的用户名和密码
3. 确保数据库 `seu_airline` 已创建

### 错误3：端口占用

**错误信息：**
```
Port 8080 is already in use
```

**解决方法：**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <进程ID> /F

# Linux/Mac
lsof -i :8080
kill -9 <进程ID>

# 或修改 application.yml 中的端口
server:
  port: 8081
```

---

## 📝 默认账户信息

系统启动后会自动创建以下默认账户：

**管理员账户：**
- 用户名: `admin`
- 密码: `admin123`
- 访问地址: `http://localhost:8080/api/auth/login`

---

## 🔍 快速测试

### 1. 测试用户注册

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "email": "test@example.com",
    "phone": "13800138000",
    "fullName": "测试用户",
    "idCard": "320123199001011234"
  }'
```

### 2. 测试用户登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### 3. 测试获取航班列表

```bash
curl -X GET "http://localhost:8080/api/flight/search?departureCity=南京&arrivalCity=北京" \
  -H "Authorization: Bearer <your_token>"
```

---

## 📚 更多信息

- 完整文档：[README.md](./README.md)
- API 文档：http://localhost:8080/api/swagger-ui/
- 项目仓库：https://github.com/SEUAirline/seu-airline-backend

---

## 🆘 需要帮助？

如果遇到其他问题，请：

1. 检查日志输出中的错误信息
2. 确认所有必需服务都已启动（MySQL、Redis）
3. 查看 `application.yml` 配置是否正确
4. 查阅项目文档或提交 Issue

---

**祝你开发顺利！** 🎉
