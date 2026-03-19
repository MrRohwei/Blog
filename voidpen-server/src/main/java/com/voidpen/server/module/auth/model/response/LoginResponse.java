package com.voidpen.server.module.auth.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;

    private UserInfoVO userInfo;
}
