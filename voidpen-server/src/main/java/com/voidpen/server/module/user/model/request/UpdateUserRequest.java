package com.voidpen.server.module.user.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过 50")
    private String username;

    @Size(min = 6, max = 64, message = "密码长度需在 6-64 位之间")
    private String password;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100")
    private String email;

    @Size(max = 255, message = "头像地址长度不能超过 255")
    private String avatar;

    @Size(max = 50, message = "昵称长度不能超过 50")
    private String nickname;

    @Pattern(regexp = "ROLE_ADMIN|ROLE_USER", message = "角色值非法")
    private String role;

    @Min(value = 0, message = "状态值非法")
    @Max(value = 1, message = "状态值非法")
    private Integer status;
}
