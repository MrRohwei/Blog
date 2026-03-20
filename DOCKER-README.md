# Voidpen 一键预览（Docker Compose）

## 1. 准备环境变量

```bash
cp .env.example .env
```

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

## 2. 一键启动

```bash
mkdir -p volumes/mysql volumes/redis volumes/uploads volumes/server-logs volumes/frontend-nginx-logs volumes/admin-nginx-logs
docker compose up -d --build
```

## 3. 访问地址

- 前台：`http://localhost:5173`
- 后台：`http://localhost:5174`
- 后端：`http://localhost:8080`
- 接口文档：`http://localhost:8080/doc.html`

默认管理员：

- 用户名：`admin`
- 密码：`admin123`

## 4. 常用命令

```bash
docker compose ps
docker compose logs -f server
docker compose down
docker compose down -v
```

## 5. 重要挂载目录

- `./volumes/mysql`：MySQL 数据
- `./volumes/redis`：Redis 数据
- `./volumes/uploads`：上传文件
- `./volumes/server-logs`：后端日志文件
- `./volumes/frontend-nginx-logs`：前台 Nginx 日志
- `./volumes/admin-nginx-logs`：后台 Nginx 日志
