package com.voidpen.server.module.system.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EchoRequest {

    @NotBlank(message = "message 不能为空")
    private String message;
}
