# ─────────────────────────────────────────────
#  voidpen-frontend/Dockerfile
# ─────────────────────────────────────────────

# ── 阶段一：Node 构建 ───────────────────────
FROM node:20-alpine AS builder

WORKDIR /build

COPY package.json package-lock.json ./
RUN npm ci --prefer-offline

COPY . .
RUN npm run build

# ── 阶段二：Nginx 静态服务 ──────────────────
FROM nginx:1.25-alpine

COPY --from=builder /build/dist /usr/share/nginx/html/

# 覆盖默认 nginx 配置（处理 SPA history 路由）
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
