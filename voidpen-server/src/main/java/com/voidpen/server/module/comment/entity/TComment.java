package com.voidpen.server.module.comment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("t_comment")
public class TComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("blog_id")
    private Long blogId;

    @TableField("user_id")
    private Long userId;

    @TableField("parent_id")
    private Long parentId;

    private String nickname;

    private String email;

    private String avatar;

    private String content;

    private Integer status;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
