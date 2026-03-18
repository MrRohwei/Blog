# Voidpen · Docker 本地一键启动指南

---

## 前提条件

| 工具 | 最低版本 | 检查命令 |
|------|----------|----------|
| Docker | 24.x | `docker --version` |
| Docker Compose | v2.x | `docker compose version` |

> macOS / Windows 安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/) 即可获得上述两者。

---

## 项目目录结构要求

启动前确认目录如下（三个子项目 + docker 配置在同一根目录）：

```
voidpen/                        ← 根目录（docker compose 在此运行）
├── docker-compose.yml
├── .env                        ← 由 .env.example 复制而来
├── docker/
│   ├── nginx/
│   │   ├── Dockerfile
│   │   └── nginx.conf
│   └── mysql/
│       └── init.sql
├── voidpen-server/             ← Spring Boot 项目（含 Dockerfile）
│   ├── Dockerfile
│   └── src/main/resources/
│       └── application-docker.yml
├── voidpen-frontend/           ← 前台 Vue 项目（含 nginx.conf）
│   ├── Dockerfile
│   └── nginx.conf
└── voidpen-admin/              ← 后台 Vue 项目（含 Dockerfile）
    └── Dockerfile
```

---

## 快速启动（3 步）

### 第一步：复制并填写环境变量

```bash
cp .env.example .env
```

`.env` 默认值可直接用于本地开发，无需修改。若需要 OSS 功能，填写对应的 Key。

### 第二步：一键启动所有服务

```bash
docker compose up -d --build
```

首次启动会拉取镜像并编译代码，大约需要 **3~8 分钟**（取决于网速和机器性能）。

### 第三步：等待服务就绪

```bash
# 查看所有服务状态
docker compose ps

# 实时查看后端启动日志
docker compose logs -f server
```

等待 `voidpen-server` 状态变为 `healthy`（约 40~60 秒）后即可访问。

---

## 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前台博客 | http://localhost | 访客访问的展示站 |
| 后台管理 | http://localhost/admin/ | 管理员后台 |
| API 文档 | http://localhost/doc.html | Knife4j 接口文档 |
| 后端直连 | http://localhost:8080 | 跳过 Nginx 直接访问 |
| MySQL | localhost:3306 | 用 DataGrip 等工具连接 |
| Redis | localhost:6379 | 用 RedisInsight 等工具连接 |

**默认管理员账号：**

| 字段 | 值 |
|------|----|
| 用户名 | `admin` |
| 密码 | `Admin@123456` |

---

## 常用命令

```bash
# 启动所有服务（后台运行）
docker compose up -d

# 重新构建并启动（代码有改动时）
docker compose up -d --build

# 仅重新构建某个服务
docker compose up -d --build server

# 停止所有服务（保留数据卷）
docker compose stop

# 停止并删除容器（保留数据卷）
docker compose down

# 彻底清除（含数据卷，慎用！）
docker compose down -v

# 查看所有服务日志
docker compose logs -f

# 查看单个服务日志
docker compose logs -f server
docker compose logs -f mysql
docker compose logs -f nginx

# 进入后端容器调试
docker compose exec server sh

# 进入 MySQL 容器
docker compose exec mysql mysql -u voidpen -p voidpen
```

---

## 热更新开发

Docker 适合一次性启动基础设施，日常开发建议：

```bash
# 1. 仅启动 MySQL + Redis（基础设施）
docker compose up -d mysql redis

# 2. 后端本地运行（热重载）
cd voidpen-server
mvn spring-boot:run

# 3. 前台本地运行（HMR）
cd voidpen-frontend
npm run dev

# 4. 后台本地运行（HMR）
cd voidpen-admin
npm run dev
```

此模式下：
- MySQL 连接地址：`localhost:3306`
- Redis 连接地址：`localhost:6379`
- 前台访问：`http://localhost:5173`
- 后台访问：`http://localhost:5174`
- 后端 API：`http://localhost:8080`

---

## 常见问题

### Q: 启动报错 `port 3306 already in use`

本机已运行 MySQL，与容器端口冲突。解决方案：
```bash
# 方案1：停止本机 MySQL
sudo systemctl stop mysql  # Linux
brew services stop mysql   # macOS

# 方案2：修改 docker-compose.yml 中 mysql 映射端口
ports:
  - "3307:3306"  # 改为 3307
```

### Q: 后端一直 unhealthy，日志报数据库连接失败

MySQL 容器可能还没完全就绪。等待 10~20 秒后 `server` 容器会自动重试，或手动重启：
```bash
docker compose restart server
```

### Q: 前台页面空白或 API 请求 404

检查前端构建时 `VITE_API_BASE_URL` 是否为空（Nginx 代理模式下应为空字符串）。

### Q: 修改代码后如何更新容器

```bash
# 只重建改动的服务，不影响其他服务
docker compose up -d --build server   # 后端改动
docker compose up -d --build nginx    # 前端改动
```

### Q: 如何重置数据库

```bash
# 删除数据卷后重新启动（会重新执行 init.sql）
docker compose down -v
docker compose up -d --build
```

---

## 数据持久化说明

| 数据 | 存储位置 | 说明 |
|------|----------|------|
| MySQL 数据 | Docker Volume `mysql_data` | `down` 不删除，`down -v` 才删除 |
| Redis 数据 | Docker Volume `redis_data` | 同上 |
| 上传的图片 | OSS（云端） | 与容器无关，不受影响 |

---

*Voidpen · 虚笔 · Docker 启动指南*
