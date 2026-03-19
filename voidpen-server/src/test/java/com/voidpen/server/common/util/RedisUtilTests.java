package com.voidpen.server.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisUtilTests {

    private RedisTemplate<String, Object> redisTemplate;

    private ValueOperations<String, Object> valueOperations;

    private HashOperations<String, Object, Object> hashOperations;

    private RedisUtil redisUtil;

    @BeforeEach
    void setUp() {
        redisTemplate = Mockito.mock(RedisTemplate.class);
        valueOperations = Mockito.mock(ValueOperations.class);
        hashOperations = Mockito.mock(HashOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        redisUtil = new RedisUtil(redisTemplate);
    }

    @Test
    void shouldSetAndGetValue() {
        when(valueOperations.get("k1")).thenReturn("v1");

        redisUtil.set("k1", "v1");
        Object result = redisUtil.get("k1");

        verify(valueOperations).set("k1", "v1");
        assertThat(result).isEqualTo("v1");
    }

    @Test
    void shouldSetValueWithTtl() {
        redisUtil.set("k2", "v2", 10, TimeUnit.MINUTES);
        verify(valueOperations).set("k2", "v2", 10, TimeUnit.MINUTES);
    }

    @Test
    void shouldHandleExpireAndIncrement() {
        when(redisTemplate.expire("k3", 5, TimeUnit.MINUTES)).thenReturn(Boolean.TRUE);
        when(valueOperations.increment("k3", 1L)).thenReturn(2L);

        boolean expired = redisUtil.expire("k3", 5, TimeUnit.MINUTES);
        long value = redisUtil.increment("k3", 1L);

        assertThat(expired).isTrue();
        assertThat(value).isEqualTo(2L);
    }

    @Test
    void shouldDeleteSingleAndMultipleKeys() {
        when(redisTemplate.delete("k4")).thenReturn(Boolean.TRUE);
        when(redisTemplate.delete(any(List.class))).thenReturn(2L);

        boolean deleted = redisUtil.delete("k4");
        long deletedCount = redisUtil.delete(List.of("k4", "k5"));

        assertThat(deleted).isTrue();
        assertThat(deletedCount).isEqualTo(2L);
    }

    @Test
    void shouldOperateHashData() {
        when(hashOperations.get("h1", "field")).thenReturn("value");
        when(hashOperations.delete("h1", "field")).thenReturn(1L);

        redisUtil.hSet("h1", "field", "value");
        Object result = redisUtil.hGet("h1", "field");
        boolean deleted = redisUtil.hDelete("h1", "field");

        verify(hashOperations).put("h1", "field", "value");
        verify(hashOperations).get("h1", "field");
        assertThat(result).isEqualTo("value");
        assertThat(deleted).isTrue();
    }
}
