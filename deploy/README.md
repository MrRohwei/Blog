# P5-T2 最简上线说明

## 1. GitHub Secrets

在仓库 `Settings -> Secrets and variables -> Actions` 新增：

- `SERVER_HOST`
- `SERVER_USER`
- `SERVER_SSH_KEY`
- `VITE_API_BASE_URL`（可选）
- `FRONTEND_BASE_URL`（可选，用于后台跳转前台）

## 2. CI/CD 工作流

- `.github/workflows/deploy-server.yml`
- `.github/workflows/deploy-frontend.yml`
- `.github/workflows/deploy-admin.yml`

推送到 `main` 后会按改动目录自动部署。

## 3. Nginx 配置

把 `deploy/nginx/voidpen.conf.example` 拷贝到服务器：

```bash
sudo cp voidpen.conf.example /etc/nginx/sites-available/voidpen
sudo ln -sf /etc/nginx/sites-available/voidpen /etc/nginx/sites-enabled/voidpen
sudo nginx -t && sudo systemctl reload nginx
```

## 4. HTTPS 证书

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d yourdomain.com -d admin.yourdomain.com
```

## 5. 快速验收

- 前台：`https://yourdomain.com`
- 后台：`https://admin.yourdomain.com`
- 文档（生产应关闭）：`https://yourdomain.com/doc.html`
