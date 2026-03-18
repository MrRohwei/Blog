# Voidpen · 虚笔 — 开发计划

> 共 5 个阶段 · 预计 12 周 · Vue 3 + Spring Boot 3.x  
> 更新日期：2026-03-18

---

## 总览

| 阶段 | 名称 | 周期 | 时长 |
|------|------|------|------|
| ① | 项目基础搭建 | Week 1 – 2 | 2 周 |
| ② | 后端核心模块 | Week 3 – 5 | 3 周 |
| ③ | 前端页面开发 | Week 5 – 8 | 4 周（与②末段并行） |
| ④ | 功能完善 & 联调 | Week 9 – 10 | 2 周 |
| ⑤ | 部署上线 & 收尾 | Week 11 – 12 | 2 周 |

### 甘特图

```
W1  W2  W3  W4  W5  W6  W7  W8  W9  W10 W11 W12
▓▓▓▓▓▓▓▓                                          ① 基础搭建
        ▓▓▓▓▓▓▓▓▓▓▓▓▓                             ② 后端核心
                ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓                 ③ 前端开发
                                ▓▓▓▓▓▓▓▓          ④ 功能完善
                                        ▓▓▓▓▓▓▓▓  ⑤ 上线收尾
```

### 关键数据

| 项目 | 数量 |
|------|------|
| 总周数 | 12 周 |
| 后端接口模块 | 8 个 |
| 前端页面 | 8 个 |
| 数据库表 | 8 张 |
| REST 接口数 | 30+ 个 |

**关键决策：**

- **先打地基** — 阶段一把 JWT 鉴权体系跑通是核心，所有模块依赖它，跳过会导致后期大量返工。
- **后端先行一周** — 阶段二和阶段三在 Week 5 起并行，后端提前约一周建好接口，前端才能顺畅对接。
- **博客模块排最长** — Redis 浏览量累加、缓存、归档聚合、多条件筛选细节较多，单独排 5 天。
- **收尾留足** — 阶段五两周专注部署与验收，上线前的环境问题往往比预期复杂。

---

## 阶段一 · 项目基础搭建（Week 1 – 2）

### 任务 1：创建三个子项目仓库 & 基础配置（2d）

**范围：** 全栈

- [ ] 创建 `blog-server`（Spring Boot 3.x 骨架）
- [ ] 创建 `blog-frontend`（Vite + Vue 3）
- [ ] 创建 `blog-admin`（Vite + Vue 3 + Element Plus）
- [ ] 初始化 Git 仓库 & 配置 `.gitignore`

### 任务 2：后端基础框架配置（3d）

**范围：** 后端

- [ ] `pom.xml` 引入所有核心依赖
- [ ] `application.yml` 多环境配置（dev / prod）
- [ ] 统一响应体 `Result<T>` + `PageResult<T>`
- [ ] 全局异常处理 `GlobalExceptionHandler` + 错误码枚举 `ErrorCode`
- [ ] 集成 Knife4j 接口文档，验证可访问 `/doc.html`

### 任务 3：数据库 & 中间件初始化（2d）

**范围：** 运维

- [ ] 执行建库建表 SQL（8 张表：`t_user` / `t_blog` / `t_category` / `t_tag` / `t_blog_tag` / `t_comment` / `t_banner` / `t_advertisement`）
- [ ] 配置 MyBatis-Plus 分页插件
- [ ] 配置 Redis 连接，封装 `RedisUtil`

### 任务 4：Spring Security + JWT 鉴权体系（3d）

**范围：** 后端  
> ⚠️ 本阶段最重要任务，其他所有模块依赖此基础。

- [ ] `SecurityConfig` 配置过滤器链（公开路由白名单）
- [ ] `JwtAuthenticationFilter` 请求拦截与 Token 解析
- [ ] `JwtUtil` 封装（生成 / 解析 / 验证签名与过期）
- [ ] Token 黑名单机制（登出后写入 Redis，TTL = Token 剩余有效期）
- [ ] 实现登录接口 `POST /admin/v1/auth/login`
- [ ] 实现登出接口 `POST /admin/v1/auth/logout`
- [ ] 实现当前用户信息接口 `GET /admin/v1/auth/info`

### 任务 5：前端 Axios 封装 & 路由骨架（2d）

**范围：** 前端

- [ ] `request.js` 封装：请求拦截器注入 `Authorization` Header，响应拦截器处理 401 跳转登录
- [ ] `blog-admin` 登录页面开发
- [ ] 路由守卫（未登录跳转 `/login`）
- [ ] `AdminLayout` 框架组件（侧边栏导航 + 顶部 Header + 面包屑）

---

## 阶段二 · 后端核心模块（Week 3 – 5）

### 任务 1：用户管理模块（2d）

- [ ] 用户列表分页查询
- [ ] 新建 / 编辑用户（密码 BCrypt 加密）
- [ ] 删除用户
- [ ] 启用 / 禁用用户状态 `PUT /admin/v1/users/{id}/status`

### 任务 2：分类 & 标签模块（2d）

- [ ] 分类 CRUD（含排序字段）
- [ ] 标签 CRUD（含颜色字段）
- [ ] 分类 / 标签列表各自返回博客数量统计

### 任务 3：博客核心模块（5d）

> 本阶段工作量最大，重点保障质量。

- [ ] 博客新建 / 编辑 / 删除
- [ ] 博客状态流转：草稿 → 发布 → 下线
- [ ] 置顶 `PUT /admin/v1/blogs/{id}/top`
- [ ] 博客-标签多对多关联（中间表维护）
- [ ] 分页 + 多条件筛选（`status` / `categoryId` / `keyword`，MyBatis XML 动态 SQL）
- [ ] 浏览量：请求详情时 Redis 原子 `INCR`，定时任务每 5 分钟同步至 MySQL
- [ ] 点赞：以 `IP:blogId` 为 Key 写入 Redis，24h 内去重
- [ ] 博客详情 Redis 缓存（TTL 10 分钟，发布/编辑后主动淘汰）
- [ ] 归档时间轴：按年月聚合 `GROUP BY YEAR/MONTH(created_at)`

### 任务 4：评论模块（3d）

- [ ] 访客评论（无需登录，需填昵称 + 邮箱）
- [ ] 登录用户评论
- [ ] `parent_id` 自关联，服务层组装树形结构返回前台
- [ ] 后台评论审核：待审 / 通过 / 拒绝 `PUT /admin/v1/comments/{id}/status`
- [ ] 后台删除评论

### 任务 5：轮播图 & 广告模块（2d）

- [ ] 轮播图 CRUD + 排序 + 启用/禁用
- [ ] 广告 CRUD + 广告位枚举过滤（`SIDEBAR` / `HEADER` / `FOOTER` / `DETAIL_BOTTOM`）
- [ ] 广告过期自动下线逻辑（查询时过滤 `expired_at < NOW()`）

### 任务 6：文件上传 & 数据看板（2d）

- [ ] `OssUtil` 封装（阿里云 OSS 或 MinIO，返回可访问 URL）
- [ ] 文件上传接口 `POST /admin/v1/files/upload`
- [ ] 数据统计概览接口（博客总数、今日访问量、评论数、用户数）
- [ ] 近 30 天访问趋势接口（折线图数据）

---

## 阶段三 · 前端页面开发（Week 5 – 8）

> Week 5 起与阶段二并行，后端接口优先完成后前端紧跟对接。

### 后台管理系统 blog-admin（8d）

- [ ] **数据看板页** `DashboardView.vue`
  - 统计卡片（博客数 / 今日访问 / 评论数 / 用户数）
  - 近 30 天访问趋势折线图（ECharts）
- [ ] **博客列表页** `BlogListView.vue`
  - 分页表格 + 多条件筛选
  - 状态标签、置顶标记、操作按钮
- [ ] **博客编辑页** `BlogEditView.vue`
  - Markdown 编辑器（md-editor-v3）
  - 封面图上传（调用 OSS 上传接口）
  - 分类选择 / 标签多选 / 状态设置
- [ ] **分类管理页** — 表格 + 新增/编辑弹窗
- [ ] **标签管理页** — 表格 + 颜色选择器
- [ ] **评论审核页** — 待审列表 + 批量通过/拒绝
- [ ] **用户管理页** — 表格 + 启用/禁用
- [ ] **轮播图管理页** — 图片预览 + 排序拖拽
- [ ] **广告管理页** — 广告位分组展示

### 前台展示系统 blog-frontend（8d）

- [ ] **首页** `HomeView.vue`
  - 轮播图（Swiper 或自实现）
  - 博客卡片列表（分页加载）
  - 侧边栏（分类 / 标签云 / 推荐博客 / 广告位）
- [ ] **博客详情页** `BlogDetailView.vue`
  - Markdown 渲染（marked.js + highlight.js）
  - 目录导航 TOC（锚点跟随高亮）
  - 点赞按钮（动画反馈）
  - 评论区（树形展示 + 访客发表表单）
- [ ] **分类页** `CategoryView.vue` — 按分类筛选博客列表
- [ ] **标签页** `TagView.vue` — 标签云 + 按标签筛选
- [ ] **归档页** `ArchiveView.vue` — 按年月展示时间轴
- [ ] **广告位组件** — 侧边栏 / 详情页底部
- [ ] 返回顶部按钮
- [ ] 404 页面

---

## 阶段四 · 功能完善 & 联调（Week 9 – 10）

### 任务 1：前后端全链路联调（3d）

- [ ] 逐接口核对字段命名、分页结构、错误码
- [ ] Nginx 跨域配置验证（本地 dev proxy → 生产环境 Nginx）
- [ ] Token 过期场景：前端静默刷新或跳转登录逻辑验证
- [ ] 大文件上传测试（封面图 > 5MB 的裁剪或压缩策略）

### 任务 2：SEO & 性能优化（3d）

- [ ] 博客详情页动态注入 `<title>` 和 `<meta name="description">`
- [ ] 图片懒加载（`loading="lazy"` 或 IntersectionObserver）
- [ ] 路由懒加载（`defineAsyncComponent` / 动态 `import()`）
- [ ] Vite 构建分包优化（vendor / md-editor 独立 chunk）
- [ ] 博客列表接口响应时间压测（目标 < 200ms）

### 任务 3：安全加固（2d）

- [ ] 评论内容服务端 + 前端双重 XSS 过滤（DOMPurify）
- [ ] 所有 POST/PUT 接口 Bean Validation 注解补全
- [ ] 点赞接口限流压测（模拟同 IP 高频请求，验证 Redis 去重生效）
- [ ] 敏感接口（用户管理 / 删除博客）二次确认弹窗

### 任务 4：Bug 修复 & 体验打磨（4d）

- [ ] 移动端响应式适配（重点：导航栏、博客列表卡片、评论区）
- [ ] 列表加载骨架屏（Skeleton 占位）
- [ ] 空状态页（无博客 / 无评论 / 搜索无结果）
- [ ] 错误状态页（接口异常时的友好提示）
- [ ] 全局 Toast / 确认弹窗交互风格统一

---

## 阶段五 · 部署上线 & 收尾（Week 11 – 12）

### 任务 1：服务器环境搭建（3d）

- [ ] 安装 JDK 17
- [ ] 安装 MySQL 8.0（配置远程访问权限 & 初始化数据库）
- [ ] 安装 Redis 7.x（配置密码 & 持久化）
- [ ] 安装 Nginx 1.24+
- [ ] 配置 `application-prod.yml`（数据库 / Redis / OSS 账号用环境变量注入，不写入代码）
- [ ] OSS Bucket 创建，配置读权限 & CORS 白名单

### 任务 2：CI/CD & 自动化部署（2d）

- [ ] 编写 GitHub Actions 工作流（`push to main` 触发）
  - 前端：`npm run build` → SCP 上传 `dist/` 到服务器
  - 后端：`mvn package -DskipTests` → SCP 上传 JAR → 重启服务
- [ ] 编写后端启动脚本 `start.sh` / `stop.sh`

```bash
# 启动命令示例
java -jar voidpen-server.jar \
  --spring.profiles.active=prod \
  --server.port=8080
```

### 任务 3：Nginx 配置 & HTTPS（2d）

- [ ] 申请 SSL 证书（Let's Encrypt / 云厂商免费证书）
- [ ] 配置 HTTPS 虚拟主机（443 端口）
- [ ] HTTP → HTTPS 301 强跳转
- [ ] 前台静态资源 `try_files` SPA 路由
- [ ] 后台静态资源 `/admin` 路径
- [ ] API 反向代理（`/api/` 和 `/admin/v1/` → `127.0.0.1:8080`）
- [ ] 开启 Gzip 压缩（`gzip_types text/html application/javascript application/json`）
- [ ] 静态资源浏览器缓存（`Cache-Control: max-age=31536000`）

```nginx
server {
    listen 443 ssl;
    server_name yourdomain.com;

    ssl_certificate     /etc/ssl/cert.pem;
    ssl_certificate_key /etc/ssl/key.pem;

    # 前台
    location / {
        root /opt/voidpen/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # 后台
    location /admin {
        alias /opt/voidpen/admin/dist;
        try_files $uri $uri/ /admin/index.html;
    }

    # 后端 API
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

### 任务 4：上线验收 & 文档整理（3d）

- [ ] 全流程冒烟测试（注册 → 发博客 → 前台展示 → 评论 → 后台审核）
- [ ] 录入初始数据（至少 3 个分类、10 个标签、首篇博客）
- [ ] 压测首页接口（目标 QPS > 100，响应 < 300ms）
- [ ] 编写 `README.md`（本地开发启动说明 + 生产部署文档）
- [ ] Knife4j 接口文档截图归档
- [ ] 关闭 Knife4j 生产环境访问（`knife4j.enable=false`）

---

## 开发规范约定

### 分支策略

```
main          生产分支（只接受 PR 合并）
dev           开发主分支
feat/xxx      功能分支（如 feat/blog-module）
fix/xxx       Bug 修复分支
```

### 提交信息格式

```
feat: 新增博客分页查询接口
fix: 修复评论树形结构组装丢失根节点的问题
refactor: 重构 JwtUtil Token 解析逻辑
docs: 更新 API 接口文档
```

### 接口联调约定

- 后端新增接口后在 Knife4j 文档标注「已完成」并通知前端
- 字段命名统一使用 `camelCase`（后端 Jackson 配置驼峰序列化）
- 时间字段统一返回 ISO 8601 格式字符串：`2026-03-18T10:30:00`
- 分页参数统一：`?page=1&size=10`（从第 1 页开始）

---

*文档生成于 2026-03-18 · Voidpen · 虚笔*