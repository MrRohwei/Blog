package com.voidpen.server.module.blog.service;

import com.voidpen.server.common.constant.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlogViewAsyncService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Async
    public void incrementViewAsync(Long blogId) {
        redisTemplate.opsForValue().increment(RedisKeys.BLOG_VIEWS + blogId);
    }
}
