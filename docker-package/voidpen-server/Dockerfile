# ─────────────────────────────────────────────
#  voidpen-server/Dockerfile
#  多阶段构建：构建阶段 + 运行阶段分离
#  最终镜像体积约 250MB
# ─────────────────────────────────────────────

# ── 阶段一：Maven 构建 ──────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# 先复制 pom.xml 单独下载依赖（利用 Docker 层缓存）
# 只要 pom.xml 不变，依赖层不会重新下载
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 再复制源码并构建
COPY src ./src
RUN mvn package -DskipTests -B

# ── 阶段二：运行镜像 ────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 安装 curl（用于 healthcheck）
RUN apk add --no-cache curl tzdata \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && apk del tzdata

# 从构建阶段复制 JAR
COPY --from=builder /build/target/voidpen-server-*.jar app.jar

# 创建非 root 用户运行（安全最佳实践）
RUN addgroup -S voidpen && adduser -S voidpen -G voidpen
USER voidpen

EXPOSE 8080

# JVM 优化参数（适合 2GB 内存容器）
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
