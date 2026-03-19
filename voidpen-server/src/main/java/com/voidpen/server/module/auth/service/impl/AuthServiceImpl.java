package com.voidpen.server.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.voidpen.server.common.constant.RedisKeys;
import com.voidpen.server.common.enums.ErrorCode;
import com.voidpen.server.common.exception.BusinessException;
import com.voidpen.server.module.auth.model.request.LoginRequest;
import com.voidpen.server.module.auth.model.request.UpdatePasswordRequest;
import com.voidpen.server.module.auth.model.response.LoginResponse;
import com.voidpen.server.module.auth.model.response.UserInfoVO;
import com.voidpen.server.module.auth.service.AuthService;
import com.voidpen.server.module.auth.util.JwtUtil;
import com.voidpen.server.module.user.entity.TUser;
import com.voidpen.server.module.user.mapper.UserMapper;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public LoginResponse login(LoginRequest request) {
        TUser user = userMapper.selectOne(
            new LambdaQueryWrapper<TUser>().eq(TUser::getUsername, request.getUsername())
        );

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        userMapper.updateById(new TUser().setId(user.getId()).setLastLoginTime(LocalDateTime.now()));

        return new LoginResponse(token, UserInfoVO.from(user));
    }

    @Override
    public void logout(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }

        long ttl = jwtUtil.getRemainingSeconds(token);
        if (ttl > 0) {
            redisTemplate.opsForValue().set(
                RedisKeys.TOKEN_BLACKLIST + token,
                "1",
                ttl,
                TimeUnit.SECONDS
            );
        }
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        TUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return UserInfoVO.from(user);
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        TUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        TUser toUpdate = new TUser()
            .setId(userId)
            .setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(toUpdate);
    }
}
