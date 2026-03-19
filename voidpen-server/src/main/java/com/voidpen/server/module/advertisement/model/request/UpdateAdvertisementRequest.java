package com.voidpen.server.module.advertisement.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UpdateAdvertisementRequest {

    @NotBlank(message = "广告标题不能为空")
    @Size(max = 100, message = "标题长度不能超过 100")
    private String title;

    @Size(max = 255, message = "图片地址长度不能超过 255")
    private String imageUrl;

    @Size(max = 255, message = "跳转地址长度不能超过 255")
    private String linkUrl;

    @NotBlank(message = "广告位不能为空")
    @Pattern(
        regexp = "SIDEBAR|HEADER|FOOTER|DETAIL_BOTTOM",
        message = "广告位值非法"
    )
    private String position;

    @Min(value = 0, message = "状态值非法")
    @Max(value = 1, message = "状态值非法")
    private Integer status;

    private LocalDateTime expiredAt;
}
