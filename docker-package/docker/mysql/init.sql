-- ─────────────────────────────────────────────
--  docker/mysql/init.sql
--  容器首次启动时自动执行（已存在数据则跳过）
-- ─────────────────────────────────────────────

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password        VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密',
    email           VARCHAR(100),
    avatar          VARCHAR(255),
    nickname        VARCHAR(50),
    role            VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
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
    user_id    BIGINT,
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
    position   VARCHAR(30)  NOT NULL,
    status     TINYINT      NOT NULL DEFAULT 1,
    expired_at DATETIME,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── 初始数据 ──────────────────────────────
-- 默认管理员账号：admin / Admin@123456
-- 密码为 BCrypt 哈希，可登录后在后台修改
INSERT IGNORE INTO t_user (username, password, nickname, role, status)
VALUES (
    'admin',
    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
    '管理员',
    'ROLE_ADMIN',
    1
);

-- 默认分类
INSERT IGNORE INTO t_category (id, name, description, sort_order) VALUES
(1, '技术',   '技术文章、编程笔记', 1),
(2, '生活',   '日常随笔、生活感悟', 2),
(3, '随笔',   '想到什么写什么',     3);

-- 默认标签
INSERT IGNORE INTO t_tag (id, name, color) VALUES
(1, 'Java',   '#f89820'),
(2, 'Vue',    '#42b883'),
(3, 'MySQL',  '#4479a1'),
(4, 'Redis',  '#dc382d'),
(5, 'Spring', '#6db33f'),
(6, '随笔',   '#909399');

SET FOREIGN_KEY_CHECKS = 1;
