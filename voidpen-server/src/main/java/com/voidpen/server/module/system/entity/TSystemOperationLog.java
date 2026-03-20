package com.voidpen.server.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("t_system_operation_log")
public class TSystemOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("operation_type")
    private String operationType;

    @TableField("target_scope")
    private String targetScope;

    private String detail;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
