# Voidpen Blog System

## 1. 项目介绍

Voidpen 是一个前后端分离的博客系统，包含：

- 博客前台展示站点（访客浏览、文章详情、评论）
- 博客后台管理系统（内容运营、配置管理）
- Spring Boot 后端服务（统一 API、鉴权、文件上传）
- MySQL + Redis 基础数据与缓存能力

项目当前支持 Docker Compose 一键启动，适合本地联调、测试环境预览和小规模部署。

## 2. 核心功能

- 用户与登录鉴权（JWT）
- 分类、标签管理
- 博客草稿/发布/下线管理
- 评论审核
- 轮播图、广告位管理
- 文件上传（本地存储）
- 后台看板统计
- OpenAPI/Knife4j 接口文档

## 3. 项目架构

- `voidpen-frontend`：前台站点（Vue 3）
- `voidpen-admin`：后台管理端（Vue 3 + Element Plus）
- `voidpen-server`：后端 API 服务（Spring Boot）
- `mysql`：业务主库
- `redis`：缓存/会话相关能力

默认端口：

- 前台：`5173`
- 后台：`5174`
- 后端：`8080`
- MySQL：`3306`
- Redis：`6379`

## 4. 技术栈

### 后端

- Java 17
- Spring Boot 3.5.0
- Spring Security + JWT
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Redis 7
- Knife4j + SpringDoc OpenAPI

### 前端

- Vue 3
- Vue Router 4
- Pinia
- Vite 8
- Axios
- Element Plus（后台）
- marked + highlight.js + dompurify（前台 Markdown 渲染）
- md-editor-v3（后台编辑器）

### 运维与部署

- Docker / Docker Compose
- Nginx（前台、后台静态资源服务与反向代理）

## 5. 项目结构说明（包结构）

```text
.
├── voidpen-server/                  # 后端服务
│   ├── src/main/java/com/voidpen/server
│   │   ├── common/                  # 通用响应、异常、工具、常量
│   │   ├── config/                  # 安全、跨域、序列化等配置
│   │   └── module/                  # 业务模块
│   │       ├── auth/                # 登录鉴权
│   │       ├── user/                # 用户管理
│   │       ├── category/            # 分类管理
│   │       ├── tag/                 # 标签管理
│   │       ├── blog/                # 博客核心
│   │       ├── comment/             # 评论管理
│   │       ├── banner/              # 轮播图管理
│   │       ├── advertisement/       # 广告管理
│   │       ├── file/                # 文件上传
│   │       ├── dashboard/           # 后台看板
│   │       └── system/              # 系统相关接口
│   ├── src/main/resources/
│   │   ├── application*.yml         # 环境配置
│   │   └── mapper/                  # MyBatis XML
│   └── sql/init.sql                 # 数据库初始化脚本
├── voidpen-frontend/                # 前台项目
├── voidpen-admin/                   # 后台项目
├── docker-compose.yml               # 一键启动编排
├── DEPLOY.md                        # 最简部署说明
├── DOCKER-README.md                 # Docker 使用说明
└── deploy/                          # 上线与 Nginx 配置示例
```

后端模块内部基本遵循：

- `controller`：接口层
- `service`：业务层
- `mapper`：数据访问层
- `entity`：实体
- `model/request`：入参 DTO
- `model/response`：出参 DTO

## 6. 快速开始（推荐：Docker Compose）

### 6.1 前置条件

- Docker 24+
- Docker Compose 插件

### 6.2 启动步骤

```bash
cp .env.example .env
# Windows PowerShell: Copy-Item .env.example .env

docker compose up -d --build
```

### 6.3 初始化说明

- MySQL 容器首次启动会自动执行：`voidpen-server/sql/init.sql`
- 会自动创建默认管理员账号：
  - 用户名：`admin`
  - 密码：`admin123`

### 6.4 访问地址

- 前台：`http://localhost:5173`
- 后台：`http://localhost:5174`
- 后端：`http://localhost:8080`
- API 文档：`http://localhost:8080/doc.html`

## 7. 本地开发模式（不走 Docker）

### 7.1 启动后端

```bash
cd voidpen-server
mvn spring-boot:run
```

默认启用 `dev` 配置，请先准备本机 MySQL 与 Redis，并按需修改：

- `voidpen-server/src/main/resources/application-dev.yml`

### 7.2 启动前台

```bash
cd voidpen-frontend
npm install
npm run dev
```

### 7.3 启动后台

```bash
cd voidpen-admin
npm install
npm run dev
```

## 8. 部署说明（服务器）

最简单方式：直接使用根目录 `docker-compose.yml`。

部署时至少需要修改：

- `.env` 中的数据库/Redis/JWT 密钥
- `.env` 中的前台地址：`FRONTEND_BASE_URL`（用于后台跳转到前台）
- `docker-compose.yml` 中后端 CORS：`VOIDPEN_CORS_ALLOWED_ORIGINS`
- `docker-compose.yml` 中上传访问地址：`VOIDPEN_OSS_BASE_URL`
- `docker-compose.yml` 中前台/后台构建参数：`VITE_API_BASE_URL`

详细上线步骤见：

- `DEPLOY.md`
- `deploy/README.md`

## 9. 重要挂载目录（建议备份）

- `volumes/mysql`：MySQL 数据
- `volumes/redis`：Redis 数据
- `volumes/uploads`：上传文件
- `volumes/server-logs`：后端日志
- `volumes/frontend-nginx-logs`：前台 Nginx 日志
- `volumes/admin-nginx-logs`：后台 Nginx 日志

## 10. 常用运维命令

```bash
docker compose ps
docker compose logs -f server
docker compose restart server
docker compose down
docker compose down -v    # 谨慎：会清理卷数据
```

## 11. 接口与规范

- 接口文档：`/doc.html`
- 统一响应结构：`{ code, message, data }`
- 分页参数：`page` 从 `1` 开始，`size` 最大 `100`
- 时间格式：`yyyy-MM-dd HH:mm:ss`

## 12. 常见问题

### 12.1 `doc.html` 打不开或报错

检查：

- 后端容器是否正常：`docker compose ps`
- 后端日志：`docker compose logs -f server`
- 是否错误访问了旧路径（建议直接访问 `http://localhost:8080/doc.html`）

### 12.2 跨域问题

修改 `docker-compose.yml` 中：

- `VOIDPEN_CORS_ALLOWED_ORIGINS`

并重建后端：

```bash
docker compose up -d --build server
```

### 12.3 上传文件无法访问

检查：

- `VOIDPEN_OSS_BASE_URL` 是否与实际访问域名一致
- `volumes/uploads` 是否正确挂载

## 13. 开发与提交流程建议

- 功能开发分支：`feat/*`
- 修复分支：`fix/*`
- 合并到主分支前至少完成：
  - 本地构建通过
  - 核心页面冒烟测试
  - 接口文档关键路径可访问

## 14. Roadmap（建议）

- 单元测试与集成测试补齐
- 生产环境日志集中采集
- 多环境配置模板（staging/prod）
- 对象存储（OSS/S3）适配

---

如果你只想快速预览：按「第 6 节」执行即可。
