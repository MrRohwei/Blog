package com.voidpen.server.module.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("t_blog")
public class TBlog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("category_id")
    private Long categoryId;

    private String title;

    private String content;

    @TableField("cover_img")
    private String coverImg;

    private String summary;

    private Integer views;

    private Integer likes;

    private Integer status;

    @TableField("is_top")
    private Integer isTop;

    @TableField("is_featured")
    private Integer isFeatured;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
