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
