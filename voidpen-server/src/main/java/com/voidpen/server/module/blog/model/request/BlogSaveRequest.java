package com.voidpen.server.module.blog.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class BlogSaveRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过 200")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @Size(max = 255, message = "封面地址长度不能超过 255")
    private String coverImg;

    @Size(max = 500, message = "摘要长度不能超过 500")
    private String summary;

    @Min(value = 0, message = "状态值非法")
    @Max(value = 2, message = "状态值非法")
    private Integer status;

    @Min(value = 0, message = "置顶值非法")
    @Max(value = 1, message = "置顶值非法")
    private Integer isTop;

    @Min(value = 0, message = "推荐值非法")
    @Max(value = 1, message = "推荐值非法")
    private Integer isFeatured;

    private List<Long> tagIds;
}
