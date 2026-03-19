package com.voidpen.server.module.system.controller;

import com.voidpen.server.common.response.Result;
import com.voidpen.server.module.system.model.request.EchoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统接口")
@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    @Operation(summary = "服务连通性检查")
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }

    @Operation(summary = "参数校验演示接口")
    @PostMapping("/echo")
    public Result<String> echo(@Valid @RequestBody EchoRequest request) {
        return Result.success(request.getMessage());
    }
}
