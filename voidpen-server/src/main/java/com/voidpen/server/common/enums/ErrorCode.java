package com.voidpen.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或 Token 已失效"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    SYSTEM_ERROR(500, "服务器内部错误"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已被禁用"),
    PASSWORD_ERROR(1003, "密码错误"),
    USERNAME_DUPLICATE(1004, "用户名已存在"),

    BLOG_NOT_FOUND(2001, "博客不存在"),
    BLOG_ALREADY_LIKED(2002, "今天已经点过赞了"),
    CATEGORY_NOT_FOUND(3001, "分类不存在"),
    CATEGORY_DUPLICATE(3002, "分类名称已存在"),
    CATEGORY_HAS_BLOGS(3003, "该分类下存在博客，无法删除"),

    TAG_NOT_FOUND(4001, "标签不存在"),
    TAG_DUPLICATE(4002, "标签名称已存在"),

    COMMENT_NOT_FOUND(5001, "评论不存在"),
    COMMENT_GUEST_INFO_REQUIRED(5002, "访客昵称和邮箱不能为空"),

    BANNER_NOT_FOUND(6001, "轮播图不存在"),

    ADVERTISEMENT_NOT_FOUND(7001, "广告不存在"),
    ADVERTISEMENT_POSITION_INVALID(7002, "广告位参数非法"),

    FILE_TOO_LARGE(8001, "文件大小不能超过 5MB"),
    FILE_UPLOAD_FAILED(8002, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(8003, "仅支持上传 JPG/PNG/GIF/WebP 图片");

    private final Integer code;

    private final String message;
}
