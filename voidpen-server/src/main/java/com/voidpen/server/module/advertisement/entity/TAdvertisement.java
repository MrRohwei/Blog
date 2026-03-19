package com.voidpen.server.module.advertisement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("t_advertisement")
public class TAdvertisement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    @TableField("image_url")
    private String imageUrl;

    @TableField("link_url")
    private String linkUrl;

    private String position;

    private Integer status;

    @TableField("expired_at")
    private LocalDateTime expiredAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
