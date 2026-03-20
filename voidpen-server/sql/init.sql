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

-- 初始管理员账号（admin123）
INSERT INTO t_user (username, password, nickname, role, status)
SELECT 'admin', '$2a$10$.KvG7EENAfCtYJ/HLoAyce6YomL1x0PbBl66WScKjHb8/H8ORPbF6', '管理员', 'ROLE_ADMIN', 1
WHERE NOT EXISTS (SELECT 1 FROM t_user WHERE username = 'admin');
