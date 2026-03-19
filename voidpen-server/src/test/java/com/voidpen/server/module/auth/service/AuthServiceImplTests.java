package com.voidpen.server.module.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.voidpen.server.common.constant.RedisKeys;
import com.voidpen.server.common.enums.ErrorCode;
import com.voidpen.server.common.exception.BusinessException;
import com.voidpen.server.module.auth.model.request.LoginRequest;
import com.voidpen.server.module.auth.model.response.LoginResponse;
import com.voidpen.server.module.auth.model.response.UserInfoVO;
import com.voidpen.server.module.auth.service.impl.AuthServiceImpl;
import com.voidpen.server.module.auth.util.JwtUtil;
import com.voidpen.server.module.user.entity.TUser;
import com.voidpen.server.module.user.mapper.UserMapper;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceImplTests {

    private UserMapper userMapper;

    private PasswordEncoder passwordEncoder;

    private JwtUtil jwtUtil;

    private RedisTemplate<String, Object> redisTemplate;

    private ValueOperations<String, Object> valueOperations;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        userMapper = Mockito.mock(UserMapper.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        jwtUtil = Mockito.mock(JwtUtil.class);
        redisTemplate = Mockito.mock(RedisTemplate.class);
        valueOperations = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        authService = new AuthServiceImpl(userMapper, passwordEncoder, jwtUtil, redisTemplate);
    }

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        TUser user = new TUser()
            .setId(1L)
            .setUsername("admin")
            .setPassword("encoded")
            .setRole("ROLE_ADMIN")
            .setStatus(1)
            .setNickname("管理员");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("admin123", "encoded")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "admin", "ROLE_ADMIN")).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUserInfo().getUsername()).isEqualTo("admin");
        verify(userMapper).updateById(any(TUser.class));
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("not-found");
        request.setPassword("123");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.USER_NOT_FOUND.getCode());
    }

    @Test
    void shouldThrowWhenPasswordMismatch() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("bad");

        TUser user = new TUser().setId(1L).setUsername("admin").setPassword("encoded").setStatus(1);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("bad", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.PASSWORD_ERROR.getCode());
    }

    @Test
    void shouldThrowWhenUserDisabled() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        TUser user = new TUser()
            .setId(1L)
            .setUsername("admin")
            .setPassword("encoded")
            .setStatus(0);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("admin123", "encoded")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BusinessException.class)
            .extracting("code")
            .isEqualTo(ErrorCode.USER_DISABLED.getCode());
    }

    @Test
    void shouldAddTokenToBlacklistOnLogout() {
        when(jwtUtil.getRemainingSeconds("token")).thenReturn(120L);

        authService.logout("token");

        verify(valueOperations).set(eq(RedisKeys.TOKEN_BLACKLIST + "token"), eq("1"), eq(120L), eq(TimeUnit.SECONDS));
    }

    @Test
    void shouldReturnCurrentUserInfo() {
        TUser user = new TUser()
            .setId(2L)
            .setUsername("user")
            .setRole("ROLE_USER")
            .setStatus(1);
        when(userMapper.selectById(2L)).thenReturn(user);

        UserInfoVO info = authService.getUserInfo(2L);

        assertThat(info.getId()).isEqualTo(2L);
        assertThat(info.getUsername()).isEqualTo("user");
    }
}
