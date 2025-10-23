# JWT与Redis整合 - 更新说明

## 📝 更新内容

### 1. 新增依赖
- **Spring Data Redis**: Spring Boot的Redis集成
- **Commons Pool2**: Redis连接池支持

### 2. 新增配置类
- `RedisConfig.java`: Redis序列化配置

### 3. 新增服务类
- `RedisService.java`: Redis操作封装服务

### 4. 更新的类

#### JwtUtils.java
- ✅ 整合Redis存储Token
- ✅ 实现Token黑名单机制
- ✅ 支持Token刷新
- ✅ 增强Token验证逻辑

#### AuthController.java
- ✅ 新增 `/auth/logout` 登出接口
- ✅ 新增 `/auth/refresh` Token刷新接口

### 5. 配置文件更新
在 `application.yml` 中添加Redis配置:
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
```

## 🚀 快速开始

### 前置要求
1. **安装Redis**
   ```bash
   # Windows: 下载 Redis for Windows
   # https://github.com/tporadowski/redis/releases
   
   # 启动Redis
   redis-server.exe
   ```

2. **验证Redis运行**
   ```bash
   redis-cli ping
   # 应返回: PONG
   ```

### 启动应用
```bash
mvn clean install
mvn spring-boot:run
```

### 运行测试
```powershell
# 使用提供的测试脚本
.\test-jwt-redis.ps1
```

## 🔧 核心功能

### 1. Token存储
所有生成的Token都存储在Redis中:
- **Key格式**: `jwt:token:{username}`
- **过期时间**: 与JWT配置一致(默认24小时)
- **自动清理**: Redis自动删除过期Token

### 2. Token黑名单
登出或刷新Token时,旧Token加入黑名单:
- **Key格式**: `jwt:blacklist:{token}`
- **过期时间**: Token的剩余有效期
- **验证拦截**: 黑名单Token无法通过验证

### 3. Token刷新
用户可以在Token过期前刷新:
- 生成新Token
- 旧Token加入黑名单
- 返回新Token信息

### 4. 登出功能
用户主动登出:
- Token加入黑名单
- 从有效Token列表移除
- 立即失效

## 📡 API接口

### 登录 (获取Token)
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

### 登出
```http
POST /api/auth/logout
Authorization: Bearer {token}
```

### 刷新Token
```http
POST /api/auth/refresh
Authorization: Bearer {token}
```

### 获取当前用户信息
```http
GET /api/auth/me
Authorization: Bearer {token}
```

## 🔍 Redis数据查看

```bash
# 查看所有有效Token
redis-cli keys "jwt:token:*"

# 查看所有黑名单Token
redis-cli keys "jwt:blacklist:*"

# 查看特定用户Token
redis-cli get "jwt:token:admin"

# 查看Token剩余时间(秒)
redis-cli ttl "jwt:token:admin"

# 监控所有Redis操作
redis-cli monitor
```

## 📊 工作流程

### 登录流程
```
用户登录 → 验证身份 → 生成Token → 存储到Redis → 返回Token
```

### Token验证流程
```
接收请求 → 提取Token → 检查黑名单 → 验证签名 → 检查Redis存储 → 放行/拒绝
```

### 登出流程
```
接收登出请求 → 提取Token → 加入黑名单 → 删除有效Token → 返回成功
```

### 刷新流程
```
接收刷新请求 → 验证旧Token → 生成新Token → 旧Token入黑名单 → 新Token存Redis → 返回新Token
```

## ✨ 优势

1. **安全性**
   - ✅ 支持主动登出
   - ✅ Token立即失效
   - ✅ 防止Token盗用
   - ✅ 单点登录控制

2. **性能**
   - ✅ Redis快速查询
   - ✅ 减少数据库压力
   - ✅ 自动过期清理

3. **灵活性**
   - ✅ Token刷新机制
   - ✅ 黑名单管理
   - ✅ 在线用户统计
   - ✅ 会话管理

## 🛠️ 故障排除

### Redis连接失败
```
错误: Unable to connect to Redis
解决: 
1. 检查Redis是否启动
2. 检查application.yml配置
3. 检查防火墙设置
```

### Token验证失败
```
可能原因:
1. Token已过期
2. Token在黑名单中
3. Redis中无此Token
4. Token签名错误
```

### 应用重启后Token失效
```
原因: Redis默认不持久化
解决: 配置Redis持久化(RDB/AOF)或要求用户重新登录
```

## 📚 相关文档

- [JWT-REDIS-INTEGRATION.md](./JWT-REDIS-INTEGRATION.md) - 详细使用指南
- [test-jwt-redis.ps1](./test-jwt-redis.ps1) - 自动化测试脚本

## 🔜 未来优化

1. **Token自动续期**: 用户活跃时自动延长有效期
2. **多设备管理**: 支持同时登录多个设备
3. **强制登出**: 管理员强制用户下线
4. **登录历史**: 记录用户登录日志
5. **IP限制**: IP白名单/黑名单
6. **限流保护**: 防止暴力破解
7. **分布式会话**: 支持多实例部署

## 🤝 贡献

如有问题或建议,请提交Issue或Pull Request。

---

**版本**: 1.0.0  
**更新日期**: 2025-10-23  
**作者**: SEUAirline Team
