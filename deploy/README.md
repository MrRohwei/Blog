# 生产上线（手动部署）

本目录仅保留 Nginx 站点配置示例，不再包含 GitHub 自动部署流程。

## 1. 准备站点目录

以 `/opt/voidpen` 为例：

```bash
sudo mkdir -p /opt/voidpen/frontend
sudo mkdir -p /opt/voidpen/admin
sudo mkdir -p /opt/voidpen/server
```

## 2. 部署前台/后台静态资源

将本地构建产物上传到服务器对应目录：

- 前台 `voidpen-frontend/dist/*` -> `/opt/voidpen/frontend`
- 后台 `voidpen-admin/dist/*` -> `/opt/voidpen/admin`

## 3. 配置 Nginx

使用 `deploy/nginx/voidpen.conf.example`：

```bash
sudo cp deploy/nginx/voidpen.conf.example /etc/nginx/sites-available/voidpen
sudo ln -sf /etc/nginx/sites-available/voidpen /etc/nginx/sites-enabled/voidpen
sudo nginx -t && sudo systemctl reload nginx
```

按实际域名、后端地址调整配置后再重载。

## 4. 配置 HTTPS

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d yourdomain.com -d admin.yourdomain.com
```

## 5. 快速验收

- 前台：`https://yourdomain.com`
- 后台：`https://admin.yourdomain.com`
- 后端：`https://yourdomain.com/api/...`（经反向代理）
