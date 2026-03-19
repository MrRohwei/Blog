# voidpen-server

Voidpen 博客系统后端服务，基于 Spring Boot 3 和 Java 17。

## 环境要求

- JDK 17
- Maven 3.9+
- MySQL 8.x（后续任务接入）
- Redis 7.x（后续任务接入）

## 本地启动

```bash
mvn spring-boot:run
```

默认端口：`8080`

默认环境：`dev`（配置文件：`application-dev.yml`）

接口文档：`http://localhost:8080/doc.html`

数据库初始化脚本：`sql/init.sql`

## 打包

```bash
mvn clean package
```
