FROM node:20-alpine AS builder

WORKDIR /build

COPY package.json package-lock.json ./
RUN npm ci --prefer-offline

COPY . .

ARG VITE_API_BASE_URL=http://localhost:8080
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ARG VITE_FRONTEND_BASE_URL=http://localhost:5173
ENV VITE_FRONTEND_BASE_URL=${VITE_FRONTEND_BASE_URL}

RUN npm run build

FROM nginx:latest

COPY --from=builder /build/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
