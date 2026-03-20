# Voidpen 最简部署文档

这个版本只追求「能跑起来」，适合先快速上线预览。

## 1. 服务器准备

- 一台 Linux 服务器（推荐 Ubuntu 22.04）
- 已安装 Docker + Docker Compose 插件

```bash
docker -v
docker compose version
```

## 2. 拉代码并配置环境变量

```bash
git clone <你的仓库地址> blog
cd blog
cp .env.example .env
```

按需修改 `.env`（至少改这几个）：

- `MYSQL_ROOT_PASSWORD`
- `MYSQL_PASSWORD`
- `REDIS_PASSWORD`
- `JWT_SECRET`
- `FRONTEND_BASE_URL`（后台跳转前台地址）

## 3. 一键启动

```bash
mkdir -p volumes/mysql volumes/redis volumes/uploads volumes/server-logs volumes/frontend-nginx-logs volumes/admin-nginx-logs
docker compose up -d --build
```

查看状态：

```bash
docker compose ps
docker compose logs -f server
```

## 4. 访问地址

假设服务器 IP 是 `x.x.x.x`：

- 前台：`http://x.x.x.x:5173`
- 后台：`http://x.x.x.x:5174`
- 后端：`http://x.x.x.x:8080`
- 文档：`http://x.x.x.x:8080/doc.html`

默认管理员账号：

- 用户名：`admin`
- 密码：`admin123`

## 5. 日常维护（最常用）

更新代码并重启：

```bash
git pull
docker compose up -d --build
```

停止：

```bash
docker compose down
```

彻底清空（会删数据库数据）：

```bash
docker compose down -v
```

## 6. 生产上线（CI/CD + Nginx）

见：`deploy/README.md`

## 7. 挂载目录（建议备份）

- `volumes/mysql`
- `volumes/redis`
- `volumes/uploads`
- `volumes/server-logs`
