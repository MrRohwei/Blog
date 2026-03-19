package com.voidpen.server.module.category.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCategoryRequest {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过 50")
    private String name;

    @Size(max = 200, message = "分类描述长度不能超过 200")
    private String description;

    @Min(value = 0, message = "排序值不能小于 0")
    private Integer sortOrder;

    @Min(value = 0, message = "状态值非法")
    @Max(value = 1, message = "状态值非法")
    private Integer status;
}
