package com.voidpen.server.module.user.service;

import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.module.user.model.request.CreateUserRequest;
import com.voidpen.server.module.user.model.request.UpdateUserRequest;
import com.voidpen.server.module.user.model.request.UpdateUserStatusRequest;
import com.voidpen.server.module.user.model.request.UserQueryRequest;
import com.voidpen.server.module.user.model.response.UserVO;

public interface UserService {

    PageResult<UserVO> listUsers(UserQueryRequest request);

    void createUser(CreateUserRequest request);

    void updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    void updateUserStatus(Long id, UpdateUserStatusRequest request);
}
