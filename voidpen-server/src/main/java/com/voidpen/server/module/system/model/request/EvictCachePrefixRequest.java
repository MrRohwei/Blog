package com.voidpen.server.module.system.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EvictCachePrefixRequest {

    @NotBlank(message = "缓存前缀不能为空")
    private String prefix;
}
