# JWT与Redis整合测试指南

## 功能概述

本次更新整合了JWT和Redis,实现了以下功能:

1. **Token存储管理**: 所有有效的JWT token都存储在Redis中
2. **Token黑名单**: 实现登出功能,将已登出的token加入黑名单
3. **Token刷新**: 支持刷新token,延长用户会话
4. **单点登录**: 同一用户同时只能有一个有效token(可选配置)

## 前置条件

### 1. 安装Redis

确保已安装并启动Redis服务:

```bash
# Windows (使用Redis for Windows)
# 下载地址: https://github.com/tporadowski/redis/releases
# 启动: 双击redis-server.exe

# Linux
sudo apt-get install redis-server
sudo systemctl start redis

# macOS
brew install redis
brew services start redis
```

### 2. 验证Redis连接

```bash
redis-cli ping
# 应该返回: PONG
```

## 配置说明

在 `application.yml` 中已添加Redis配置:

```yaml
spring:
  redis:
    host: localhost      # Redis服务器地址
    port: 6379          # Redis端口
    password:           # Redis密码(如果有)
    database: 0         # 使用的数据库编号
    timeout: 6000ms     # 连接超时时间
    lettuce:
      pool:
        max-active: 8   # 连接池最大连接数
        max-wait: -1ms  # 连接池最大阻塞等待时间
        max-idle: 8     # 连接池最大空闲连接
        min-idle: 0     # 连接池最小空闲连接
```

## API测试

### 1. 用户登录 (获取Token)

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**响应示例:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "admin",
  "role": "ADMIN"
}
```

**Redis存储验证:**
```bash
redis-cli
> keys jwt:token:*
1) "jwt:token:admin"
> get jwt:token:admin
"eyJhbGciOiJIUzUxMiJ9..."
> ttl jwt:token:admin
(integer) 86395  # 剩余秒数
```

### 2. 访问受保护资源

使用获取的token访问API:

```http
GET http://localhost:8080/api/auth/me
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### 3. 用户登出 (Token加入黑名单)

```http
POST http://localhost:8080/api/auth/logout
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**响应:**
```json
"登出成功!"
```

**Redis验证:**
```bash
redis-cli
> keys jwt:blacklist:*
1) "jwt:blacklist:eyJhbGciOiJIUzUxMiJ9..."
> keys jwt:token:admin
(empty list or set)  # token已被删除
```

登出后,使用该token访问API将被拒绝。

### 4. 刷新Token

在token过期前刷新获取新token:

```http
POST http://localhost:8080/api/auth/refresh
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**响应示例:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",  // 新token
  "type": "Bearer",
  "id": 1,
  "username": "admin",
  "role": "ADMIN"
}
```

**行为:**
- 旧token被加入黑名单
- 生成新token并存储到Redis
- 旧token立即失效

## Redis数据结构

### Token存储
- **Key**: `jwt:token:{username}`
- **Value**: JWT token字符串
- **TTL**: 与JWT过期时间相同(默认24小时)

### Token黑名单
- **Key**: `jwt:blacklist:{token}`
- **Value**: 用户名
- **TTL**: Token的剩余有效时间

## 工作流程

### 登录流程
```
1. 用户提交登录信息
2. 验证用户名密码
3. 生成JWT token
4. 将token存储到Redis (key: jwt:token:{username})
5. 返回token给客户端
```

### Token验证流程
```
1. 从请求头获取token
2. 检查token是否在黑名单中
3. 验证token签名和有效期
4. 从Redis获取该用户的有效token
5. 比对token是否一致
6. 验证通过,放行请求
```

### 登出流程
```
1. 从请求头获取token
2. 将token加入黑名单
3. 从Redis删除有效token
4. 返回登出成功
```

### Token刷新流程
```
1. 验证旧token有效性
2. 将旧token加入黑名单
3. 生成新token
4. 将新token存储到Redis
5. 返回新token
```

## 优势

1. **安全性提升**
   - 支持主动登出,token立即失效
   - 防止token被盗用后长期使用
   - 支持单点登录

2. **灵活性**
   - 可以随时撤销特定用户的所有token
   - 支持token刷新,提升用户体验
   - 可以查询在线用户

3. **性能优化**
   - Redis快速验证token有效性
   - 减少数据库查询压力

## 常见问题

### 1. Redis连接失败

**错误信息:** `Unable to connect to Redis`

**解决方案:**
- 检查Redis服务是否启动
- 检查application.yml中的Redis配置
- 检查防火墙设置

### 2. Token验证失败

**可能原因:**
- Redis中没有对应的token记录
- Token已被加入黑名单
- Token已过期
- Token签名错误

### 3. 重启应用后token失效

这是正常行为,因为Redis中的数据会在应用重启时清空(默认配置)。

**解决方案:**
- 配置Redis持久化(RDB或AOF)
- 或者要求用户重新登录

## 下一步优化建议

1. **Token自动续期**: 在用户活跃时自动延长token有效期
2. **多设备管理**: 记录用户的所有登录设备
3. **强制登出**: 管理员可以强制特定用户登出
4. **登录日志**: 记录用户登录历史
5. **IP白名单**: 限制特定IP访问
6. **限流**: 防止暴力破解

## 测试检查清单

- [ ] Redis服务正常启动
- [ ] 成功登录并获取token
- [ ] Token存储到Redis
- [ ] 使用token访问受保护资源
- [ ] 登出后token失效
- [ ] 黑名单token无法使用
- [ ] 成功刷新token
- [ ] 旧token在刷新后失效
- [ ] 新token正常使用

## 监控建议

使用Redis命令监控系统状态:

```bash
# 查看所有token
redis-cli keys "jwt:token:*"

# 查看所有黑名单token
redis-cli keys "jwt:blacklist:*"

# 查看在线用户数
redis-cli keys "jwt:token:*" | wc -l

# 监控Redis操作
redis-cli monitor
```
