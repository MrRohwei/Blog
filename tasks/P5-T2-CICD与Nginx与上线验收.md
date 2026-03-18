# P5-T2 · CI/CD 自动部署 & Nginx HTTPS 配置 & 上线验收

| 字段 | 内容 |
|------|------|
| 阶段 | 阶段五 · 部署上线 & 收尾 |
| 周期 | Week 11 – 12 |
| 预估工时 | 7 天 |
| 负责范围 | 运维 / 全栈 |
| 前置依赖 | P5-T1 |

---

## 任务目标

配置 GitHub Actions 自动化部署流水线，完成 Nginx HTTPS 配置，完成全站上线验收测试并整理项目文档。

---

## 模块一：CI/CD 自动化部署（2d）

### 后端 GitHub Actions

```yaml
# .github/workflows/deploy-server.yml
name: Deploy Server

on:
  push:
    branches: [main]
    paths: ['voidpen-server/**']

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build with Maven
        working-directory: ./voidpen-server
        run: mvn package -DskipTests

      - name: Deploy to Server
        uses: appleboy/scp-action@v0.1.7
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          source: "voidpen-server/target/voidpen-server.jar"
          target: "/opt/voidpen/server/"
          strip_components: 2

      - name: Restart Service
        uses: appleboy/ssh-action@v1.0.3
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          script: |
            /opt/voidpen/server/stop.sh || true
            sleep 2
            /opt/voidpen/server/start.sh
            sleep 5
            curl -f http://localhost:8080/admin/v1/auth/info || exit 1
```

### 前台前端 GitHub Actions

```yaml
# .github/workflows/deploy-frontend.yml
name: Deploy Frontend

on:
  push:
    branches: [main]
    paths: ['voidpen-frontend/**']

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup Node
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: voidpen-frontend/package-lock.json

      - name: Build
        working-directory: ./voidpen-frontend
        run: |
          npm ci
          npm run build

      - name: Deploy
        uses: appleboy/scp-action@v0.1.7
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          source: "voidpen-frontend/dist/*"
          target: "/opt/voidpen/frontend/"
          strip_components: 2
```

> 后台 `voidpen-admin` 创建同样的 workflow，target 改为 `/opt/voidpen/admin/`。

### GitHub Secrets 配置

在仓库 Settings → Secrets and variables → Actions 中添加：

| Secret 名称 | 说明 |
|-------------|------|
| `SERVER_HOST` | 服务器公网 IP |
| `SERVER_USER` | SSH 用户名（如 ubuntu） |
| `SERVER_SSH_KEY` | SSH 私钥内容 |

---

## 模块二：Nginx HTTPS 配置（2d）

### 申请 SSL 证书（Let's Encrypt）

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d yourdomain.com -d admin.yourdomain.com
```

### Nginx 配置文件

```nginx
# /etc/nginx/sites-available/voidpen
# HTTPS 主配置
server {
    listen 443 ssl http2;
    server_name yourdomain.com;

    ssl_certificate     /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_ciphers         HIGH:!aNULL:!MD5;

    # Gzip 压缩
    gzip on;
    gzip_types text/html text/css application/javascript application/json image/svg+xml;
    gzip_min_length 1024;

    # 前台静态资源（SPA）
    location / {
        root /opt/voidpen/frontend/dist;
        try_files $uri $uri/ /index.html;
        # 静态资源长缓存
        location ~* \.(js|css|png|jpg|gif|ico|woff2)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }

    # 后端 API 代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 30s;
        proxy_read_timeout 60s;
    }

    location /admin/v1/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# 后台管理系统
server {
    listen 443 ssl http2;
    server_name admin.yourdomain.com;

    ssl_certificate     /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

    location / {
        root /opt/voidpen/admin/dist;
        try_files $uri $uri/ /index.html;
    }

    location /admin/v1/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

# HTTP → HTTPS 强跳转
server {
    listen 80;
    server_name yourdomain.com admin.yourdomain.com;
    return 301 https://$host$request_uri;
}
```

```bash
# 测试配置并重载
sudo nginx -t
sudo systemctl reload nginx

# 设置证书自动续签（Let's Encrypt 90 天有效期）
sudo crontab -e
# 添加：0 3 * * * certbot renew --quiet && systemctl reload nginx
```

---

## 模块三：上线验收 & 文档整理（3d）

### 上线前冒烟测试清单

**前台访问验证：**
- [ ] `https://yourdomain.com` 正常访问，显示首页
- [ ] HTTP 访问自动跳转 HTTPS
- [ ] 浏览器地址栏显示锁形图标（SSL 有效）
- [ ] 博客列表正常加载，有测试数据
- [ ] 博客详情页正常渲染（Markdown + 代码高亮）
- [ ] 点赞、评论功能正常（评论提交后待审核状态）

**后台访问验证：**
- [ ] `https://admin.yourdomain.com` 正常访问登录页
- [ ] 管理员账号可正常登录
- [ ] 数据看板统计数据正常显示
- [ ] 可新建、编辑、发布博客
- [ ] 评论审核通过后前台可见
- [ ] 文件上传功能正常（图片上传至 OSS）

**安全验证：**
- [ ] `https://yourdomain.com/doc.html` 返回 404 或 403（Knife4j 已在生产关闭）
- [ ] 直接访问 `/admin/v1/blogs` 无 Token 返回 401
- [ ] 服务器 3306 端口对外不开放（`nmap yourdomain.com -p 3306` 无响应）
- [ ] 服务器 6379 端口对外不开放

**性能验证：**
- [ ] 使用 `curl -w "%{time_total}" https://yourdomain.com/api/v1/blogs` 响应时间 < 1s
- [ ] Chrome Lighthouse 跑一次，Performance 得分 > 65

### 录入初始数据

- [ ] 新建至少 3 个分类（如：技术、生活、随笔）
- [ ] 新建至少 8 个标签（如：Java、Vue、MySQL、Redis、随笔、读书、旅行、美食）
- [ ] 上传至少 1 张轮播图
- [ ] 发布至少 3 篇博客（内容含标题、正文、封面、分类、标签）

### 整理 README.md

```markdown
# Voidpen · 虚笔

> 一个基于 Vue 3 + Spring Boot 3.x 的个人博客系统

## 技术栈
- 前台：Vue 3 + Vite + Pinia
- 后台：Vue 3 + Element Plus
- 后端：Spring Boot 3.x + MyBatis-Plus + Spring Security + JWT
- 数据库：MySQL 8.0 + Redis 7.x
- 存储：阿里云 OSS

## 本地开发

### 启动后端
\`\`\`bash
cd voidpen-server
mvn spring-boot:run
\`\`\`

### 启动前台
\`\`\`bash
cd voidpen-frontend
npm install && npm run dev
\`\`\`

### 启动后台
\`\`\`bash
cd voidpen-admin
npm install && npm run dev
\`\`\`

## 生产部署
参见 docs/deploy.md
```

### 生产环境关闭 Knife4j

```yaml
# application-prod.yml
knife4j:
  enable: false
```

---

## 验收标准

- [ ] GitHub Actions 推送代码后自动部署成功（Actions 页面显示绿色 ✓）
- [ ] 前台和后台均通过 HTTPS 访问，无证书警告
- [ ] SSL 证书自动续签 crontab 任务已配置
- [ ] 全站冒烟测试清单 100% 通过
- [ ] 初始数据录入完毕，首页有博客展示
- [ ] `README.md` 包含项目介绍、技术栈、本地启动说明
- [ ] 生产环境 `/doc.html` 接口文档已关闭
- [ ] 服务器数据库端口（3306、6379）对外不暴露
