package com.voidpen.server.module.blog.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateBlogTopRequest {

    @NotNull(message = "置顶状态不能为空")
    @Min(value = 0, message = "置顶值非法")
    @Max(value = 1, message = "置顶值非法")
    private Integer isTop;
}
