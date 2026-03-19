package com.voidpen.server.module.tag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("t_tag")
public class TTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String color;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
