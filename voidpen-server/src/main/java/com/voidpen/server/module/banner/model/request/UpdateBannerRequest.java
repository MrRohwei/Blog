package com.voidpen.server.module.banner.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateBannerRequest {

    @Size(max = 100, message = "标题长度不能超过 100")
    private String title;

    @NotBlank(message = "图片地址不能为空")
    @Size(max = 255, message = "图片地址长度不能超过 255")
    private String imageUrl;

    @Size(max = 255, message = "跳转地址长度不能超过 255")
    private String linkUrl;

    @Min(value = 0, message = "排序值不能小于 0")
    private Integer sortOrder;

    @Min(value = 0, message = "状态值非法")
    @Max(value = 1, message = "状态值非法")
    private Integer status;
}
