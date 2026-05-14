# LearnBuddy — 基于位置的校园自习社交小程序

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.10 / Spring Cloud 2023.0.0 / Spring Cloud Alibaba 2023.0.3.4 |
| 注册 & 配置中心 | Nacos 3.1 |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis（GEO、ZSET、令牌存储） |
| 认证 | JWT + Spring Security 无状态会话 |
| ORM | MyBatis-Plus 3.5.15 |
| 对象存储 | 阿里云 OSS |
| 前端 | 微信小程序原生框架 |
| 构建 | Maven（Jenkins 自动构建） |

## 项目结构

```
LearnBuddy/
├── lb-common/          # 共享模块（JWT、安全配置、异常处理、工具类）
├── lb-api/             # Feign 接口契约（跨服务调用）
├── lb-gateway/         # API 网关（8081，动态路由）
├── lb-user/            # 用户服务（8082）
├── lb-location/        # 位置服务（8083，Redis GEO）
├── lb-websocket/       # WebSocket 聊天（8084）
├── lb-invitation/      # 邀约 + 自习计时 + 聊天记录（8085）
├── lb-material/        # 资料上传（8086，OSS）
├── LearnBuddy-miniProgram/  # 微信小程序前端
├── sql/                # 建库建表 SQL
├── nacos共享配置/       # Nacos 共享配置文件
└── pom.xml             # 父 POM
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6+
- Nacos 3.1（Docker 部署：`ip:8848/nacos`，默认账号 `nacos/nacos`）

### 2. 创建数据库

执行 `sql/` 目录下的 SQL 文件创建对应数据库和表：

| SQL 文件 | 对应库 | 对应服务 |
|----------|--------|----------|
| `01_user.sql` | `lb_user` | lb-user |
| `02_location.sql` | `lb_location` | lb-location |
| `03_invitation.sql` | `lb_invitation` | lb-invitation |
| `04_material.sql` | `lb_material` | lb-material |

> **注意**：必须先创建数据库再启动服务，库名对应各模块 `bootstrap.yaml` 中的 `datasource.name`。

### 3. 加载 Nacos 共享配置

将 `nacos共享配置/` 目录下的所有 `.yaml` 文件导入 Nacos（命名空间 `public`，组 `DEFAULT_GROUP`）：

| 配置文件 | 说明 |
|----------|------|
| `shared-jdbc.yaml` | 数据库连接（`${mysql.MYSQL_HOST}` / `${mysql.MYSQL_PASSWORD}` 占位符，启动时从 `bootstrap-dev.yml` 注入） |
| `shared-redis.yaml` | Redis 连接（`${redis.host}` / `${redis.port}` 占位符） |
| `shared-jwt-properties.yaml` | JWT 密钥和过期时间 |
| `shared-security.yaml` | Spring Security 白名单 |
| `shared-wechat-properties.yaml` | 微信小程序 AppId / Secret |
| `shared-oss-properties.yaml` | 阿里云 OSS 凭证 |
| `shared-MybatisPlus.yaml` | MyBatis-Plus 全局配置 |
| `gateway-routers.yaml` | API 网关动态路由 |

> **重要**：这些配置文件中的凭证（如 JWT secret、微信 AppId、OSS AK/SK）需要替换为你自己的真实值。

### 4. 修改远程服务地址

各模块的 `bootstrap-dev.yml` 中修改以下参数：

```yaml
server.host: <你的服务器IP>
mysql:
  MYSQL_PASSWORD: <你的MySQL密码>
  MYSQL_HOST: <你的MySQL地址>
nacos:
  username: <你的Nacos账号>
  password: <你的Nacos密码>
```

### 5. 构建并启动

```bash
# 项目根目录
mvn clean install -DskipTests

# 启动顺序：gateway > common 已打包 > 各业务模块
# 依次启动 lb-gateway, lb-user, lb-location, lb-websocket, lb-invitation, lb-material
```

### 6. 小程序前端

微信开发者工具打开 `LearnBuddy-miniProgram/` 目录。修改 `miniprogram/utils/config.js` 中的后端地址：

```js
const HOST = '<你的服务器IP>';
```

## 外部依赖清单

| 依赖 | 版本 | 用途 | 必需 |
|------|------|------|------|
| MySQL | 8.0+ | 持久化存储 | 是 |
| Redis | 6+ | 缓存、GEO、ZSET、令牌 | 是 |
| Nacos | 3.1 | 服务注册与配置中心 | 是 |
| 阿里云 OSS | — | 文件/头像存储 | 是 |
| 微信小程序 | — | 前端运行环境 | 是 |

## 注意事项

1. **Nacos 必须先启动**，所有服务依赖 Nacos 做服务发现和配置加载
2. **Redis 必须先启动**，JWT 令牌验证依赖 Redis 存储，lb-location 依赖 Redis GEO
3. **lb-location 首次启动**：`LocationDataLoader` 会在启动时将 `room` 表数据加载到 Redis Hash 和 GEO set，确保 `room` 表已有数据
4. **网关路由**：路由配置在 Nacos `gateway-routers.yaml` 中动态加载，修改后无需重启网关
5. **测试登录**：`POST /user/loginTest` 不走微信 API，传 `userId` 或 `name` 即可获取 JWT 令牌，用于 ApiFox/Postman 联调

## 测试接口（ApiFox）

```json
// 登录获取 token
POST /user/loginTest
{
    "name": "测试用户"
}
// 返回 { "code": 1, "data": { "id": "xxx", "token": "xxx", "name": "测试用户" } }
//
// 后续所有请求 Header 带 Authorization: Bearer <token>
```

## 模块端口

| 模块 | 端口 | 说明 |
|------|------|------|
| lb-gateway | 8081 | API 网关（前端统一入口） |
| lb-user | 8082 | 用户登录/登出/信息 |
| lb-location | 8083 | 位置管理（GEO） |
| lb-websocket | 8084 | WebSocket 聊天 |
| lb-invitation | 8085 | 邀约/自习/聊天记录 |
| lb-material | 8086 | 资料上传（OSS） |
