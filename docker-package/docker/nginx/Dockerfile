# ─────────────────────────────────────────────
#  docker/nginx/Dockerfile
#  将前台、后台各自 build 后打入同一个 Nginx 镜像
# ─────────────────────────────────────────────

# ── 构建前台 ────────────────────────────────
FROM node:20-alpine AS frontend-builder
WORKDIR /build
COPY voidpen-frontend/package.json voidpen-frontend/package-lock.json ./
RUN npm ci --prefer-offline
COPY voidpen-frontend/ .
RUN npm run build

# ── 构建后台 ────────────────────────────────
FROM node:20-alpine AS admin-builder
WORKDIR /build
COPY voidpen-admin/package.json voidpen-admin/package-lock.json ./
RUN npm ci --prefer-offline
COPY voidpen-admin/ .
RUN npm run build

# ── 最终 Nginx 镜像 ─────────────────────────
FROM nginx:1.25-alpine

# 前台产物
COPY --from=frontend-builder /build/dist /usr/share/nginx/html/frontend/

# 后台产物
COPY --from=admin-builder /build/dist /usr/share/nginx/html/admin/

# Nginx 配置
COPY docker/nginx/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
