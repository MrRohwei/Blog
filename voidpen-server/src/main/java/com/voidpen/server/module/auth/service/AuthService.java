package com.voidpen.server.module.auth.service;

import com.voidpen.server.module.auth.model.request.LoginRequest;
import com.voidpen.server.module.auth.model.request.UpdatePasswordRequest;
import com.voidpen.server.module.auth.model.response.LoginResponse;
import com.voidpen.server.module.auth.model.response.UserInfoVO;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void logout(String token);

    UserInfoVO getUserInfo(Long userId);

    void updatePassword(Long userId, UpdatePasswordRequest request);
}
