package com.voidpen.server.module.tag.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTagRequest {

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 30, message = "标签名称长度不能超过 30")
    private String name;

    @Size(max = 20, message = "标签颜色长度不能超过 20")
    private String color;
}
