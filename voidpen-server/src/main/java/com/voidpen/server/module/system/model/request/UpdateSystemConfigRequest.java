package com.voidpen.server.module.system.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSystemConfigRequest {

    @NotBlank(message = "配置分组不能为空")
    private String configGroup;

    @NotBlank(message = "配置键不能为空")
    private String configKey;

    @NotBlank(message = "配置值不能为空")
    private String configValue;
}
