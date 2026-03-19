package com.voidpen.server.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.voidpen.server.common.enums.ErrorCode;
import com.voidpen.server.common.exception.BusinessException;
import com.voidpen.server.common.response.PageResult;
import com.voidpen.server.module.user.entity.TUser;
import com.voidpen.server.module.user.mapper.UserMapper;
import com.voidpen.server.module.user.model.request.CreateUserRequest;
import com.voidpen.server.module.user.model.request.UpdateUserRequest;
import com.voidpen.server.module.user.model.request.UpdateUserStatusRequest;
import com.voidpen.server.module.user.model.request.UserQueryRequest;
import com.voidpen.server.module.user.model.response.UserVO;
import com.voidpen.server.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<UserVO> listUsers(UserQueryRequest request) {
        Page<TUser> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<TUser> wrapper = new LambdaQueryWrapper<TUser>()
            .like(StringUtils.hasText(request.getKeyword()), TUser::getUsername, request.getKeyword())
            .orderByDesc(TUser::getCreatedAt);
        IPage<TUser> pageData = userMapper.selectPage(page, wrapper);
        return PageResult.of(pageData.convert(UserVO::from));
    }

    @Override
    public void createUser(CreateUserRequest request) {
        checkDuplicateUsername(request.getUsername(), null);

        TUser user = new TUser()
            .setUsername(request.getUsername())
            .setPassword(passwordEncoder.encode(request.getPassword()))
            .setEmail(request.getEmail())
            .setAvatar(request.getAvatar())
            .setNickname(request.getNickname())
            .setRole(resolveRole(request.getRole()))
            .setStatus(request.getStatus() == null ? 1 : request.getStatus());
        userMapper.insert(user);
    }

    @Override
    public void updateUser(Long id, UpdateUserRequest request) {
        TUser existing = getRequiredUser(id);
        checkDuplicateUsername(request.getUsername(), id);

        TUser toUpdate = new TUser()
            .setId(id)
            .setUsername(request.getUsername())
            .setEmail(request.getEmail())
            .setAvatar(request.getAvatar())
            .setNickname(request.getNickname())
            .setRole(resolveRole(request.getRole(), existing.getRole()))
            .setStatus(request.getStatus() == null ? existing.getStatus() : request.getStatus());
        if (StringUtils.hasText(request.getPassword())) {
            toUpdate.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userMapper.updateById(toUpdate);
    }

    @Override
    public void deleteUser(Long id) {
        getRequiredUser(id);
        userMapper.deleteById(id);
    }

    @Override
    public void updateUserStatus(Long id, UpdateUserStatusRequest request) {
        getRequiredUser(id);
        userMapper.updateById(new TUser().setId(id).setStatus(request.getStatus()));
    }

    private TUser getRequiredUser(Long id) {
        TUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private void checkDuplicateUsername(String username, Long excludeId) {
        LambdaQueryWrapper<TUser> wrapper = new LambdaQueryWrapper<TUser>().eq(TUser::getUsername, username);
        if (excludeId != null) {
            wrapper.ne(TUser::getId, excludeId);
        }
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.USERNAME_DUPLICATE);
        }
    }

    private String resolveRole(String role) {
        return resolveRole(role, DEFAULT_ROLE);
    }

    private String resolveRole(String role, String fallbackRole) {
        if (StringUtils.hasText(role)) {
            return role;
        }
        return StringUtils.hasText(fallbackRole) ? fallbackRole : DEFAULT_ROLE;
    }
}
