package com.voidpen.server.module.blog.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BlogQueryRequest {

    @Min(value = 1, message = "页码最小为 1")
    private Long page = 1L;

    @Min(value = 1, message = "每页数量最小为 1")
    @Max(value = 100, message = "每页数量最大为 100")
    private Long size = 10L;

    @Min(value = 0, message = "状态值非法")
    @Max(value = 2, message = "状态值非法")
    private Integer status;

    private Long categoryId;

    private Long tagId;

    private String keyword;
}
