package com.voidpen.server.module.auth.util;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTests {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "voidpen-secret-key-should-be-at-least-32-bytes");
        ReflectionTestUtils.setField(jwtUtil, "expireSeconds", 3600L);
    }

    @Test
    void shouldGenerateAndParseToken() {
        String token = jwtUtil.generateToken(1L, "admin", "ROLE_ADMIN");
        Claims claims = jwtUtil.parseToken(token);

        assertThat(token).contains(".");
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("username", String.class)).isEqualTo("admin");
        assertThat(claims.get("role", String.class)).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void shouldReturnPositiveRemainingSeconds() {
        String token = jwtUtil.generateToken(1L, "admin", "ROLE_ADMIN");
        long remaining = jwtUtil.getRemainingSeconds(token);

        assertThat(remaining).isGreaterThan(0);
        assertThat(remaining).isLessThanOrEqualTo(3600L);
    }
}
