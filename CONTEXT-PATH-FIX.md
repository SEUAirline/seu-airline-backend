# Context Path 配置说明

## 问题总结

由于在 `application.yml` 中配置了 `context-path: /api`,导致了路径重复问题。

## 配置原理

### application.yml 配置
```yaml
server:
  servlet:
    context-path: /api
```

这个配置意味着所有的请求都会加上 `/api` 前缀。

### 路径组成

```
完整URL = 服务器地址 + Context Path + Controller Path + Method Path

示例:
http://localhost:8080/api/auth/login
└─────────────────┘ └─┘ └───┘ └────┘
    服务器地址       CP  Ctrl   方法
```

## 修复内容

### 1. Controller 路径 (已修复 ✅)

**修改前 (错误):**
```java
@RequestMapping("/api/auth")  // ❌ 导致路径变成 /api/api/auth
@RequestMapping("/api/admin") // ❌ 导致路径变成 /api/api/admin
```

**修改后 (正确):**
```java
@RequestMapping("/auth")   // ✅ 实际路径: /api/auth
@RequestMapping("/admin")  // ✅ 实际路径: /api/admin
```

### 2. Security 配置 (已修复 ✅)

**修改前 (错误):**
```java
.antMatchers("/api/auth/**").permitAll()  // ❌ 匹配不到
```

**修改后 (正确):**
```java
.antMatchers("/auth/**").permitAll()  // ✅ 正确匹配
```

## 修复的文件列表

✅ **Controller 层 (7个文件):**
- `AuthController.java` - `/api/auth` → `/auth`
- `AdminController.java` - `/api/admin` → `/admin`
- `AirportController.java` - `/api/airport` → `/airport`
- `FlightController.java` - `/api/flight` → `/flight`
- `OrderController.java` - `/api/orders` → `/orders`
- `SeatController.java` - `/api/seats` → `/seats`
- `StaffController.java` - `/api/staff` → `/staff`

✅ **Security 配置 (1个文件):**
- `WebSecurityConfig.java` - 所有路径移除 `/api` 前缀

## API 路径对照表

| Controller        | 旧配置         | 新配置     | 实际访问URL                             |
| ----------------- | -------------- | ---------- | --------------------------------------- |
| AuthController    | `/api/auth`    | `/auth`    | `http://localhost:8080/api/auth/...`    |
| AdminController   | `/api/admin`   | `/admin`   | `http://localhost:8080/api/admin/...`   |
| AirportController | `/api/airport` | `/airport` | `http://localhost:8080/api/airport/...` |
| FlightController  | `/api/flight`  | `/flight`  | `http://localhost:8080/api/flight/...`  |
| OrderController   | `/api/orders`  | `/orders`  | `http://localhost:8080/api/orders/...`  |
| SeatController    | `/api/seats`   | `/seats`   | `http://localhost:8080/api/seats/...`   |
| StaffController   | `/api/staff`   | `/staff`   | `http://localhost:8080/api/staff/...`   |

## 前端访问方式

**前端不需要修改!** 继续使用带 `/api` 前缀的URL:

```javascript
// 正确 ✅
axios.post('/api/auth/login', {...})
axios.get('/api/flights', {...})
axios.post('/api/orders', {...})

// 或完整URL
axios.post('http://localhost:8080/api/auth/login', {...})
```

## 测试验证

### 1. 重启应用

修改后必须重启Spring Boot应用!

```bash
# 停止当前应用 (Ctrl+C)
# 重新启动
mvn spring-boot:run
```

### 2. 测试登录接口

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**预期响应 (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "admin",
  "role": "ADMIN"
}
```

### 3. 运行测试脚本

```powershell
.\test-jwt-redis.ps1
```

**预期输出:**
```
=== JWT与Redis整合测试 ===

测试1: 用户登录
✓ 登录成功!
Token: eyJhbGciOiJIUzUxMiJ9...
用户: admin, 角色: ADMIN
...
```

## 关键要点

1. **Context Path 只配置一次** - 在 `application.yml` 中
2. **Controller 不要重复** - `@RequestMapping` 中不要包含 `/api`
3. **Security 匹配相对路径** - `antMatchers` 匹配的是相对于 Context Path 的路径
4. **前端访问完整路径** - 包含 `/api` 前缀

## 常见错误

### ❌ 错误示例 1: Controller 重复前缀
```java
// 错误!
@RequestMapping("/api/auth")
// 实际路径会变成: /api/api/auth (404错误)
```

### ❌ 错误示例 2: Security 配置错误
```java
// 错误!
.antMatchers("/api/auth/**").permitAll()
// 实际路径是 /auth/**, 匹配不到 (403错误)
```

### ✅ 正确示例
```java
// Controller
@RequestMapping("/auth")  // 相对路径

// Security
.antMatchers("/auth/**").permitAll()  // 相对路径

// 实际访问
http://localhost:8080/api/auth/login  // 完整路径
```

## 下一步

修改完成后:
1. ✅ 重启应用
2. ✅ 运行测试脚本验证
3. ✅ 测试前端集成

---

**修改完成,请重启应用后测试!** 🚀
