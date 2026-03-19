package com.voidpen.server.module.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_blog_tag")
public class TBlogTag {

    @TableField("blog_id")
    private Long blogId;

    @TableField("tag_id")
    private Long tagId;
}
