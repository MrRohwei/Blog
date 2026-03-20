CREATE DATABASE IF NOT EXISTS voidpen_dev
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE voidpen_dev;

SET NAMES utf8mb4;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password        VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密',
    email           VARCHAR(100),
    avatar          VARCHAR(255),
    nickname        VARCHAR(50),
    role            VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER' COMMENT 'ROLE_ADMIN / ROLE_USER',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    last_login_time DATETIME,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 分类表
CREATE TABLE IF NOT EXISTS t_category (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(200),
    sort_order  INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 博客表
CREATE TABLE IF NOT EXISTS t_blog (
    id          BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    category_id BIGINT,
    title       VARCHAR(200)  NOT NULL,
    content     LONGTEXT      NOT NULL,
    cover_img   VARCHAR(255),
    summary     VARCHAR(500),
    views       INT           NOT NULL DEFAULT 0,
    likes       INT           NOT NULL DEFAULT 0,
    status      TINYINT       NOT NULL DEFAULT 0 COMMENT '0=草稿 1=已发布 2=已下线',
    is_top      TINYINT       NOT NULL DEFAULT 0,
    is_featured TINYINT       NOT NULL DEFAULT 0,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 标签表
CREATE TABLE IF NOT EXISTS t_tag (
    id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(30) NOT NULL UNIQUE,
    color      VARCHAR(20),
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 博客标签中间表
CREATE TABLE IF NOT EXISTS t_blog_tag (
    blog_id BIGINT NOT NULL,
    tag_id  BIGINT NOT NULL,
    PRIMARY KEY (blog_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评论表
CREATE TABLE IF NOT EXISTS t_comment (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    blog_id    BIGINT       NOT NULL,
    user_id    BIGINT       COMMENT 'NULL=访客',
    parent_id  BIGINT       DEFAULT NULL,
    nickname   VARCHAR(50),
    email      VARCHAR(100),
    avatar     VARCHAR(255),
    content    TEXT         NOT NULL,
    status     TINYINT      NOT NULL DEFAULT 0 COMMENT '0=待审核 1=已通过 2=已拒绝',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_blog_id (blog_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 轮播图表
CREATE TABLE IF NOT EXISTS t_banner (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(100),
    image_url  VARCHAR(255) NOT NULL,
    link_url   VARCHAR(255),
    sort_order INT          NOT NULL DEFAULT 0,
    status     TINYINT      NOT NULL DEFAULT 1,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 广告表
CREATE TABLE IF NOT EXISTS t_advertisement (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(100) NOT NULL,
    image_url  VARCHAR(255),
    link_url   VARCHAR(255),
    position   VARCHAR(30)  NOT NULL COMMENT 'SIDEBAR/HEADER/FOOTER/DETAIL_BOTTOM',
    status     TINYINT      NOT NULL DEFAULT 1,
    expired_at DATETIME     COMMENT 'NULL=长期有效',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 系统配置表
CREATE TABLE IF NOT EXISTS t_system_config (
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    config_group VARCHAR(50)  NOT NULL,
    config_key   VARCHAR(100) NOT NULL,
    config_value TEXT,
    value_type   VARCHAR(20)  NOT NULL DEFAULT 'string',
    description  VARCHAR(255),
    editable     TINYINT      NOT NULL DEFAULT 1,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uniq_group_key (config_group, config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 系统运维操作日志表
CREATE TABLE IF NOT EXISTS t_system_operation_log (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    operator_id    BIGINT,
    operation_type VARCHAR(50)  NOT NULL,
    target_scope   VARCHAR(200) NOT NULL,
    detail         VARCHAR(500),
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO t_system_config (config_group, config_key, config_value, value_type, description, editable)
SELECT 'site', 'title', 'Voidpen Blog', 'string', '站点标题', 1
WHERE NOT EXISTS (
    SELECT 1 FROM t_system_config WHERE config_group = 'site' AND config_key = 'title'
);

INSERT INTO t_system_config (config_group, config_key, config_value, value_type, description, editable)
SELECT 'site', 'subtitle', '记录与分享', 'string', '站点副标题', 1
WHERE NOT EXISTS (
    SELECT 1 FROM t_system_config WHERE config_group = 'site' AND config_key = 'subtitle'
);

INSERT INTO t_system_config (config_group, config_key, config_value, value_type, description, editable)
SELECT 'seo', 'keywords', 'Voidpen,博客,技术分享', 'string', 'SEO 关键词', 1
WHERE NOT EXISTS (
    SELECT 1 FROM t_system_config WHERE config_group = 'seo' AND config_key = 'keywords'
);

INSERT INTO t_system_config (config_group, config_key, config_value, value_type, description, editable)
SELECT 'comment', 'autoAudit', 'false', 'boolean', '评论自动审核', 1
WHERE NOT EXISTS (
    SELECT 1 FROM t_system_config WHERE config_group = 'comment' AND config_key = 'autoAudit'
);

INSERT INTO t_system_config (config_group, config_key, config_value, value_type, description, editable)
SELECT 'upload', 'maxSizeMb', '10', 'number', '上传文件大小限制（MB）', 0
WHERE NOT EXISTS (
    SELECT 1 FROM t_system_config WHERE config_group = 'upload' AND config_key = 'maxSizeMb'
);

INSERT INTO t_system_config (config_group, config_key, config_value, value_type, description, editable)
SELECT 'feature', 'enableRegister', 'true', 'boolean', '开启前台注册功能', 1
WHERE NOT EXISTS (
    SELECT 1 FROM t_system_config WHERE config_group = 'feature' AND config_key = 'enableRegister'
);

-- 初始管理员账号（admin123）
INSERT INTO t_user (username, password, nickname, role, status)
SELECT 'admin', '$2a$10$.KvG7EENAfCtYJ/HLoAyce6YomL1x0PbBl66WScKjHb8/H8ORPbF6', '管理员', 'ROLE_ADMIN', 1
WHERE NOT EXISTS (SELECT 1 FROM t_user WHERE username = 'admin');
