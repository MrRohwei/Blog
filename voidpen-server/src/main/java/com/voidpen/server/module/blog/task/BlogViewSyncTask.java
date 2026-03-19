package com.voidpen.server.module.blog.task;

import com.voidpen.server.common.constant.RedisKeys;
import com.voidpen.server.module.blog.mapper.BlogMapper;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlogViewSyncTask {

    private final RedisTemplate<String, Object> redisTemplate;

    private final BlogMapper blogMapper;

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void syncViewsToDb() {
        Set<String> keys = redisTemplate.keys(RedisKeys.BLOG_VIEWS + "*");
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }

        for (String key : keys) {
            Long blogId = parseBlogId(key);
            if (blogId == null) {
                continue;
            }

            Object value = redisTemplate.opsForValue().getAndDelete(key);
            long increment = parseIncrement(value);
            if (increment > 0) {
                blogMapper.incrementViews(blogId, increment);
            }
        }
    }

    private Long parseBlogId(String key) {
        try {
            return Long.parseLong(key.replace(RedisKeys.BLOG_VIEWS, ""));
        } catch (NumberFormatException ex) {
            log.warn("Ignore invalid blog view key: {}", key);
            return null;
        }
    }

    private long parseIncrement(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }
}
