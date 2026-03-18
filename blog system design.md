# 个人博客系统（Voidpen·虚笔） · 架构设计文档

> 版本：v1.0.0 · 技术栈：Vue 3 + Spring Boot 3.x · 更新日期：2026-03-18

---

## 目录

1. [项目概述](#1-项目概述)
2. [整体技术架构](#2-整体技术架构)
3. [技术选型](#3-技术选型)
4. [数据库设计](#4-数据库设计)
5. [项目目录结构](#5-项目目录结构)
6. [核心 API 接口设计](#6-核心-api-接口设计)
7. [安全设计](#7-安全设计)
8. [部署方案](#8-部署方案)

---

## 1. 项目概述

本项目为前后端分离的个人博客系统，分为三个子应用：

| 子应用 | 说明 | 访问对象 |
|--------|------|----------|
| `blog-frontend` | 前台展示站 | 所有访客 |
| `blog-admin` | 后台管理系统 | 管理员 |
| `blog-server` | Spring Boot 后端服务 | 前台 + 后台（REST API） |

**核心功能模块：**

- **前台**：博客列表/详情、分类、标签、归档时间轴、评论、点赞、广告位、轮播图
- **后台**：博客管理（Markdown 编辑）、分类/标签管理、评论审核、用户管理、轮播图管理、广告管理、数据看板

---

## 2. 整体技术架构

```
┌─────────────────────────────────────────────────────────────────┐
│                            客 户 端                              │
│                                                                 │
│   ┌─────────────────────────┐   ┌─────────────────────────┐    │
│   │      前台展示系统         │   │      后台管理系统         │    │
│   │  Vue 3 · Vite · Pinia   │   │  Vue 3 · Element Plus   │    │
│   └────────────┬────────────┘   └────────────┬────────────┘    │
└────────────────┼────────────────────────────┼─────────────────┘
                 │                            │
                 ▼                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                          接 入 层                                │
│                                                                 │
│              ┌──────────────────────────────┐                  │
│              │            Nginx             │                  │
│              │  反向代理 · SSL · 静态资源托管  │                  │
│              └──────────────┬───────────────┘                  │
└─────────────────────────────┼───────────────────────────────────┘
                              │  REST API · JWT
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                          应 用 层                                │
│                                                                 │
│         ┌──────────────────────────────────────────┐           │
│         │        Spring Boot 3.x 后端服务            │           │
│         │  Spring Security · JWT · MyBatis-Plus     │           │
│         │  Knife4j · Validation · Redis Client      │           │
│         └──────────┬───────────┬───────────┬────────┘           │
└────────────────────┼───────────┼───────────┼────────────────────┘
                     │           │           │
          ┌──────────▼──┐  ┌────▼────┐  ┌──▼──────────┐
          │  MySQL 8.0  │  │ Redis   │  │ OSS 对象存储 │
          │   主数据库   │  │  缓存   │  │ 图片/文件    │
          └─────────────┘  └─────────┘  └─────────────┘
```

### 请求链路说明

```
访客请求
  → Nginx（SSL 终止、静态资源直出）
  → Spring Boot（JWT 验证 → 业务逻辑）
  → Redis 缓存层（热点博客详情、访问计数）
  → MySQL（持久化读写）
  → 响应 JSON → 前端渲染
```

---

## 3. 技术选型

### 3.1 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.4+ | 前台 & 后台核心框架 |
| Vite | 5.x | 构建工具 |
| Vue Router | 4.x | 路由管理 |
| Pinia | 2.x | 状态管理 |
| Element Plus | 2.x | 后台 UI 组件库 |
| md-editor-v3 | — | Markdown 编辑器（后台写博客） |
| Axios | 1.x | HTTP 请求封装 |
| highlight.js | — | 代码高亮（前台） |
| DOMPurify | — | XSS 过滤（渲染 Markdown） |

### 3.2 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 运行环境（LTS） |
| Spring Boot | 3.x | 核心框架 |
| Spring Security | 6.x | 认证与授权 |
| MyBatis-Plus | 3.5+ | ORM + 分页 |
| JWT (jjwt) | 0.12+ | Token 生成/验证 |
| Redis (Lettuce) | 7.x | 缓存客户端 |
| Knife4j | 4.x | Swagger 增强文档 |
| Validation | — | 参数校验 |
| MapStruct | 1.5+ | DTO/VO 映射 |
| Hutool | 5.x | 工具库 |

### 3.3 数据库 & 中间件

| 技术 | 版本 | 用途 |
|------|------|------|
| MySQL | 8.0 | 主数据库 |
| Redis | 7.x | 缓存 · Token 黑名单 · 限流计数 |
| Nginx | 1.24+ | 反向代理 · SSL · 静态资源 |
| OSS | — | 图片/文件存储（阿里云 OSS 或 MinIO） |

---

## 4. 数据库设计

### 4.1 ER 关系图

```
T_USER ──────< T_BLOG >──────── T_CATEGORY
                 │
                 ├──────< T_BLOG_TAG >────── T_TAG
                 │
                 └──────< T_COMMENT >──────── T_USER
                              │
                              └──(self) parent_id

T_BANNER          (独立，无外键)
T_ADVERTISEMENT   (独立，无外键)
```

### 4.2 表结构详细设计

#### t_user · 用户表

```sql
CREATE TABLE t_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username      VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    password      VARCHAR(100) NOT NULL COMMENT '密码（BCrypt）',
    email         VARCHAR(100) COMMENT '邮箱',
    avatar        VARCHAR(255) COMMENT '头像URL',
    nickname      VARCHAR(50)  COMMENT '昵称',
    role          VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER' COMMENT '角色：ROLE_ADMIN / ROLE_USER',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=启用',
    last_login_time DATETIME   COMMENT '最后登录时间',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

#### t_category · 分类表

```sql
CREATE TABLE t_category (
    id          BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name        VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称',
    description VARCHAR(200) COMMENT '分类描述',
    sort_order  INT         NOT NULL DEFAULT 0 COMMENT '排序（升序）',
    status      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=启用',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客分类表';
```

#### t_blog · 博客表

```sql
CREATE TABLE t_blog (
    id           BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '博客ID',
    user_id      BIGINT        NOT NULL COMMENT '作者ID',
    category_id  BIGINT        COMMENT '分类ID',
    title        VARCHAR(200)  NOT NULL COMMENT '标题',
    content      LONGTEXT      NOT NULL COMMENT '正文（Markdown）',
    cover_img    VARCHAR(255)  COMMENT '封面图URL',
    summary      VARCHAR(500)  COMMENT '摘要（前150字自动截取或手动填写）',
    views        INT           NOT NULL DEFAULT 0 COMMENT '浏览量',
    likes        INT           NOT NULL DEFAULT 0 COMMENT '点赞数',
    status       TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0=草稿 1=已发布 2=已下线',
    is_top       TINYINT       NOT NULL DEFAULT 0 COMMENT '是否置顶',
    is_featured  TINYINT       NOT NULL DEFAULT 0 COMMENT '是否推荐',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客文章表';
```

#### t_tag · 标签表

```sql
CREATE TABLE t_tag (
    id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '标签ID',
    name       VARCHAR(30) NOT NULL UNIQUE COMMENT '标签名',
    color      VARCHAR(20) COMMENT '标签颜色（十六进制）',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';
```

#### t_blog_tag · 博客标签中间表

```sql
CREATE TABLE t_blog_tag (
    blog_id BIGINT NOT NULL COMMENT '博客ID',
    tag_id  BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (blog_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客标签关联表';
```

#### t_comment · 评论表

```sql
CREATE TABLE t_comment (
    id         BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    blog_id    BIGINT   NOT NULL COMMENT '所属博客ID',
    user_id    BIGINT   COMMENT '评论用户ID（NULL=访客）',
    parent_id  BIGINT   DEFAULT NULL COMMENT '父评论ID（NULL=顶级评论）',
    nickname   VARCHAR(50)  COMMENT '访客昵称',
    email      VARCHAR(100) COMMENT '访客邮箱',
    avatar     VARCHAR(255) COMMENT '头像URL',
    content    TEXT     NOT NULL COMMENT '评论内容',
    status     TINYINT  NOT NULL DEFAULT 0 COMMENT '状态：0=待审核 1=已通过 2=已拒绝',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_blog_id (blog_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';
```

#### t_banner · 轮播图表

```sql
CREATE TABLE t_banner (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '轮播图ID',
    title      VARCHAR(100) COMMENT '标题',
    image_url  VARCHAR(255) NOT NULL COMMENT '图片URL',
    link_url   VARCHAR(255) COMMENT '跳转链接',
    sort_order INT          NOT NULL DEFAULT 0 COMMENT '排序（升序）',
    status     TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=启用',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';
```

#### t_advertisement · 广告表

```sql
CREATE TABLE t_advertisement (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '广告ID',
    title      VARCHAR(100) NOT NULL COMMENT '广告标题',
    image_url  VARCHAR(255) COMMENT '广告图片URL',
    link_url   VARCHAR(255) COMMENT '跳转链接',
    position   VARCHAR(30)  NOT NULL COMMENT '广告位：SIDEBAR/HEADER/FOOTER/DETAIL_BOTTOM',
    status     TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 1=启用',
    expired_at DATETIME     COMMENT '过期时间（NULL=长期有效）',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告表';
```

---

## 5. 项目目录结构

### 5.1 后端服务 `blog-server`

```
blog-server/
├── src/
│   ├── main/
│   │   ├── java/com/example/blog/
│   │   │   ├── BlogApplication.java            # 启动类
│   │   │   │
│   │   │   ├── common/                         # 公共基础
│   │   │   │   ├── result/
│   │   │   │   │   ├── Result.java             # 统一响应体 Result<T>
│   │   │   │   │   └── PageResult.java         # 分页响应体
│   │   │   │   ├── exception/
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   ├── BusinessException.java
│   │   │   │   │   └── ErrorCode.java          # 错误码枚举
│   │   │   │   ├── enums/
│   │   │   │   │   ├── BlogStatus.java
│   │   │   │   │   ├── UserRole.java
│   │   │   │   │   └── AdPosition.java
│   │   │   │   └── constants/
│   │   │   │       └── RedisKeys.java          # Redis key 常量
│   │   │   │
│   │   │   ├── config/                         # 配置类
│   │   │   │   ├── SecurityConfig.java         # Security + JWT 过滤器链
│   │   │   │   ├── RedisConfig.java            # RedisTemplate 序列化
│   │   │   │   ├── MybatisPlusConfig.java      # 分页插件
│   │   │   │   ├── OssConfig.java              # OSS 客户端 Bean
│   │   │   │   └── CorsConfig.java             # 跨域配置
│   │   │   │
│   │   │   ├── filter/
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   │
│   │   │   ├── module/                         # 业务模块（每个模块自包含）
│   │   │   │   ├── auth/
│   │   │   │   │   ├── controller/AuthController.java
│   │   │   │   │   ├── service/AuthService.java
│   │   │   │   │   ├── service/impl/AuthServiceImpl.java
│   │   │   │   │   └── dto/  LoginRequest.java / LoginResponse.java
│   │   │   │   │
│   │   │   │   ├── blog/
│   │   │   │   │   ├── controller/
│   │   │   │   │   │   ├── BlogController.java      # 前台接口
│   │   │   │   │   │   └── AdminBlogController.java # 后台接口
│   │   │   │   │   ├── service/BlogService.java
│   │   │   │   │   ├── service/impl/BlogServiceImpl.java
│   │   │   │   │   ├── mapper/BlogMapper.java
│   │   │   │   │   ├── entity/Blog.java
│   │   │   │   │   ├── dto/BlogSaveRequest.java
│   │   │   │   │   └── vo/ BlogListVO.java / BlogDetailVO.java
│   │   │   │   │
│   │   │   │   ├── category/   # 结构同 blog
│   │   │   │   ├── tag/        # 结构同 blog
│   │   │   │   ├── comment/    # 结构同 blog
│   │   │   │   ├── user/       # 结构同 blog
│   │   │   │   ├── banner/     # 结构同 blog
│   │   │   │   ├── advertisement/ # 结构同 blog
│   │   │   │   ├── file/
│   │   │   │   │   └── controller/FileController.java
│   │   │   │   └── dashboard/
│   │   │   │       ├── controller/DashboardController.java
│   │   │   │       └── vo/DashboardStatsVO.java
│   │   │   │
│   │   │   └── util/
│   │   │       ├── JwtUtil.java
│   │   │       ├── RedisUtil.java
│   │   │       └── OssUtil.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml         # 公共配置
│   │       ├── application-dev.yml     # 开发环境
│   │       ├── application-prod.yml    # 生产环境
│   │       └── mapper/                 # MyBatis XML（复杂 SQL）
│   │           └── BlogMapper.xml
│   │
│   └── test/                           # 单元测试
│
└── pom.xml
```

### 5.2 前台前端 `blog-frontend`

```
blog-frontend/
├── public/
│   └── favicon.ico
├── src/
│   ├── api/
│   │   ├── request.js          # Axios 实例 + 拦截器
│   │   ├── blog.js
│   │   ├── category.js
│   │   ├── tag.js
│   │   ├── comment.js
│   │   ├── banner.js
│   │   └── advertisement.js
│   │
│   ├── router/
│   │   └── index.js            # 路由配置
│   │
│   ├── stores/
│   │   ├── user.js
│   │   ├── blog.js
│   │   └── ui.js               # 主题、加载状态
│   │
│   ├── views/
│   │   ├── HomeView.vue        # 首页（轮播图 + 博客列表 + 侧边栏）
│   │   ├── BlogDetailView.vue  # 博客详情 + TOC + 评论
│   │   ├── ArchiveView.vue     # 归档时间轴
│   │   ├── CategoryView.vue    # 按分类筛选
│   │   ├── TagView.vue         # 按标签筛选
│   │   └── AboutView.vue       # 关于页
│   │
│   └── components/
│       ├── layout/
│       │   ├── AppHeader.vue
│       │   ├── AppFooter.vue
│       │   └── AppSidebar.vue
│       ├── blog/
│       │   ├── BlogCard.vue
│       │   ├── BlogList.vue
│       │   └── TableOfContents.vue
│       ├── comment/
│       │   ├── CommentList.vue
│       │   └── CommentForm.vue
│       └── common/
│           ├── BannerSwiper.vue
│           ├── AdvertisementWidget.vue
│           ├── AppPagination.vue
│           └── BackToTop.vue
│
├── index.html
└── vite.config.js
```

### 5.3 后台管理前端 `blog-admin`

```
blog-admin/
├── src/
│   ├── api/
│   │   ├── request.js          # Axios + Token 自动注入 + 401 跳转
│   │   ├── auth.js
│   │   ├── blog.js
│   │   ├── category.js
│   │   ├── tag.js
│   │   ├── comment.js
│   │   ├── user.js
│   │   ├── banner.js
│   │   ├── advertisement.js
│   │   ├── file.js
│   │   └── dashboard.js
│   │
│   ├── router/
│   │   └── index.js            # 动态路由 + 登录守卫
│   │
│   ├── stores/
│   │   └── user.js             # 用户信息 + Token 持久化（localStorage）
│   │
│   ├── layout/
│   │   ├── AdminLayout.vue     # 整体框架（侧边栏 + Header）
│   │   ├── AppSidebar.vue      # 折叠菜单
│   │   └── AppHeader.vue       # 顶部栏（面包屑 + 用户信息）
│   │
│   ├── views/
│   │   ├── login/
│   │   │   └── LoginView.vue
│   │   ├── dashboard/
│   │   │   └── DashboardView.vue   # 统计卡片 + 折线图
│   │   ├── blog/
│   │   │   ├── BlogListView.vue    # 博客列表管理
│   │   │   └── BlogEditView.vue    # Markdown 编辑器
│   │   ├── category/
│   │   │   └── CategoryView.vue
│   │   ├── tag/
│   │   │   └── TagView.vue
│   │   ├── comment/
│   │   │   └── CommentView.vue     # 评论审核
│   │   ├── user/
│   │   │   └── UserView.vue
│   │   ├── banner/
│   │   │   └── BannerView.vue
│   │   └── advertisement/
│   │       └── AdvertisementView.vue
│   │
│   └── utils/
│       └── auth.js             # Token 读写辅助
│
├── index.html
└── vite.config.js
```

---

## 6. 核心 API 接口设计

### 6.1 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

**分页响应 `data` 结构：**

```json
{
  "records": [],
  "total": 100,
  "page": 1,
  "size": 10,
  "pages": 10
}
```

**常用错误码：**

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 401 | 未登录 / Token 失效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

### 6.2 前台公开接口（无需鉴权）

#### 博客

```http
# 博客列表（分页 + 筛选）
GET /api/v1/blogs?page=1&size=10&categoryId=&tagId=&keyword=

# 博客详情（同时触发浏览量 +1）
GET /api/v1/blogs/{id}

# 置顶 & 推荐博客
GET /api/v1/blogs/featured

# 归档列表（按年月分组）
GET /api/v1/blogs/archive

# 博客点赞（IP 维度去重，Redis 限流）
POST /api/v1/blogs/{id}/like
```

#### 分类 & 标签

```http
# 分类列表（含各分类博客数量）
GET /api/v1/categories

# 标签列表（含各标签博客数量）
GET /api/v1/tags
```

#### 评论

```http
# 获取博客评论（树形结构，已过审）
GET /api/v1/comments/{blogId}

# 发表评论（登录用户或访客）
POST /api/v1/comments
Body: {
  "blogId": 1,
  "parentId": null,
  "content": "内容",
  "nickname": "访客昵称",   // 访客必填
  "email": "访客邮箱"       // 访客必填
}
```

#### 轮播图 & 广告

```http
# 启用中的轮播图列表
GET /api/v1/banners

# 广告列表（按广告位过滤）
GET /api/v1/advertisements?position=SIDEBAR
```

---

### 6.3 认证接口

```http
# 登录
POST /admin/v1/auth/login
Body: { "username": "admin", "password": "xxx" }
Response.data: { "token": "eyJ...", "userInfo": { "id", "username", "role", "avatar" } }

# 登出（Token 加入 Redis 黑名单）
POST /admin/v1/auth/logout
Header: Authorization: Bearer {token}

# 修改密码
PUT /admin/v1/auth/password
Body: { "oldPassword": "xxx", "newPassword": "yyy" }

# 获取当前用户信息
GET /admin/v1/auth/info
```

---

### 6.4 后台管理接口（需 Bearer Token）

#### 博客管理

```http
GET    /admin/v1/blogs                    # 博客列表，支持 ?status=&categoryId=&keyword=
POST   /admin/v1/blogs                    # 新建博客
PUT    /admin/v1/blogs/{id}               # 编辑博客
DELETE /admin/v1/blogs/{id}               # 删除博客
PUT    /admin/v1/blogs/{id}/status        # 修改状态（发布/下线）
PUT    /admin/v1/blogs/{id}/top           # 置顶 / 取消置顶

# 博客保存请求体示例
{
  "title": "文章标题",
  "content": "# Markdown 正文",
  "coverImg": "https://...",
  "summary": "摘要",
  "categoryId": 1,
  "tagIds": [1, 2, 3],
  "status": 1,
  "isTop": 0,
  "isFeatured": 0
}
```

#### 分类管理

```http
GET    /admin/v1/categories               # 列表
POST   /admin/v1/categories               # 新建
PUT    /admin/v1/categories/{id}          # 编辑
DELETE /admin/v1/categories/{id}          # 删除
```

#### 标签管理

```http
GET    /admin/v1/tags
POST   /admin/v1/tags
PUT    /admin/v1/tags/{id}
DELETE /admin/v1/tags/{id}
```

#### 评论管理

```http
GET    /admin/v1/comments?status=0        # 评论列表（status=0 待审核）
PUT    /admin/v1/comments/{id}/status     # 审核（Body: { "status": 1 }）
DELETE /admin/v1/comments/{id}            # 删除评论
```

#### 用户管理

```http
GET    /admin/v1/users                    # 用户列表
POST   /admin/v1/users                    # 新建用户
PUT    /admin/v1/users/{id}               # 编辑用户
DELETE /admin/v1/users/{id}               # 删除用户
PUT    /admin/v1/users/{id}/status        # 启用 / 禁用
```

#### 轮播图管理

```http
GET    /admin/v1/banners
POST   /admin/v1/banners
PUT    /admin/v1/banners/{id}
DELETE /admin/v1/banners/{id}
PUT    /admin/v1/banners/{id}/status
```

#### 广告管理

```http
GET    /admin/v1/advertisements
POST   /admin/v1/advertisements
PUT    /admin/v1/advertisements/{id}
DELETE /admin/v1/advertisements/{id}
PUT    /admin/v1/advertisements/{id}/status
```

#### 文件上传

```http
POST /admin/v1/files/upload
Content-Type: multipart/form-data
Form: file=<binary>
Response.data: { "url": "https://oss.../xxx.jpg" }
```

#### 数据看板

```http
# 统计数据（博客数、今日访问、评论数、用户数）
GET /admin/v1/dashboard/stats

# 近 30 天访问趋势（折线图数据）
GET /admin/v1/dashboard/trend
```

---

## 7. 安全设计

### 7.1 认证流程

```
用户登录
  → 验证 username / password（BCrypt 比对）
  → 生成 Access Token（JWT，有效期 2h）
  → 响应 Token 给前端

每次请求
  → JwtAuthenticationFilter 拦截
  → 解析 Token → 校验签名和过期时间
  → 检查 Redis 黑名单（是否已登出）
  → 注入 SecurityContext → 放行

登出
  → 将 Token 放入 Redis 黑名单（TTL = Token 剩余有效期）
```

### 7.2 接口权限矩阵

| 路径前缀 | 权限要求 |
|----------|----------|
| `/api/v1/**` | 公开，无需登录 |
| `/admin/v1/auth/login` | 公开 |
| `/admin/v1/**` | 需要 `ROLE_ADMIN` |

### 7.3 其他安全措施

- 密码使用 **BCrypt** 加密存储，不可逆
- 博客点赞接口按 **IP + 博客ID** 维度限流，防刷
- 评论提交使用 **DOMPurify** 过滤 XSS 内容
- 所有接口入参使用 **Bean Validation** 注解校验
- Nginx 层开启 **HTTPS**，HTTP 强制跳转 HTTPS

---

## 8. 部署方案

### 8.1 目录规划

```
/opt/blog/
├── server/          # Spring Boot JAR
│   └── blog-server.jar
├── frontend/        # 前台构建产物（npm run build）
│   └── dist/
├── admin/           # 后台构建产物
│   └── dist/
└── nginx/
    └── blog.conf
```

### 8.2 Nginx 配置示例

```nginx
server {
    listen 443 ssl;
    server_name yourdomain.com;

    ssl_certificate     /etc/ssl/cert.pem;
    ssl_certificate_key /etc/ssl/key.pem;

    # 前台静态资源
    location / {
        root /opt/blog/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # 后台静态资源
    location /admin {
        alias /opt/blog/admin/dist;
        try_files $uri $uri/ /admin/index.html;
    }

    # 后端 API 反向代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /admin/v1/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

server {
    listen 80;
    server_name yourdomain.com;
    return 301 https://$host$request_uri;
}
```

### 8.3 Spring Boot 启动命令

```bash
java -jar blog-server.jar \
  --spring.profiles.active=prod \
  --server.port=8080
```

---

*文档生成于 2026-03-18 · 如有调整请同步更新版本号*