package com.voidpen.server.module.user.controller;

import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.common.response.Result;
import com.voidpen.server.module.user.model.request.CreateUserRequest;
import com.voidpen.server.module.user.model.request.UpdateUserRequest;
import com.voidpen.server.module.user.model.request.UpdateUserStatusRequest;
import com.voidpen.server.module.user.model.request.UserQueryRequest;
import com.voidpen.server.module.user.model.response.UserVO;
import com.voidpen.server.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/admin/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户")
    @GetMapping
    public Result<PageResult<UserVO>> listUsers(@Valid UserQueryRequest request) {
        return Result.success(userService.listUsers(request));
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public Result<Void> createUser(@RequestBody @Valid CreateUserRequest request) {
        userService.createUser(request);
        return Result.success();
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody @Valid UpdateUserRequest request) {
        userService.updateUser(id, request);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody @Valid UpdateUserStatusRequest request) {
        userService.updateUserStatus(id, request);
        return Result.success();
    }
}
