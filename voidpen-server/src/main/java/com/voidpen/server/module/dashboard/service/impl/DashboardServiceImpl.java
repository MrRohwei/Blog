package com.voidpen.server.module.dashboard.service.impl;

import com.voidpen.server.common.constant.RedisKeys;
import com.voidpen.server.module.blog.mapper.BlogMapper;
import com.voidpen.server.module.comment.mapper.CommentMapper;
import com.voidpen.server.module.dashboard.model.response.DashboardStatsVO;
import com.voidpen.server.module.dashboard.model.response.DashboardTrendVO;
import com.voidpen.server.module.dashboard.service.DashboardService;
import com.voidpen.server.module.user.mapper.UserMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int TREND_DAYS = 30;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final BlogMapper blogMapper;

    private final CommentMapper commentMapper;

    private final UserMapper userMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public DashboardStatsVO getStats() {
        DashboardStatsVO stats = new DashboardStatsVO();
        stats.setBlogTotal(safeLong(blogMapper.selectCount(null)));
        stats.setCommentTotal(safeLong(commentMapper.selectCount(null)));
        stats.setUserTotal(safeLong(userMapper.selectCount(null)));
        stats.setTodayViews(getTodayViews());
        return stats;
    }

    @Override
    public DashboardTrendVO getTrend() {
        DashboardTrendVO trend = new DashboardTrendVO();
        LocalDate start = LocalDate.now().minusDays(TREND_DAYS - 1L);
        for (int i = 0; i < TREND_DAYS; i++) {
            LocalDate date = start.plusDays(i);
            trend.getDates().add(date.format(DATE_FORMATTER));
            trend.getViews().add(0L);
        }
        trend.getViews().set(TREND_DAYS - 1, getTodayViews());
        return trend;
    }

    private Long getTodayViews() {
        Set<String> keys = redisTemplate.keys(RedisKeys.BLOG_VIEWS + "*");
        if (CollectionUtils.isEmpty(keys)) {
            return 0L;
        }
        long total = 0L;
        for (String key : keys) {
            total += parseLong(redisTemplate.opsForValue().get(key));
        }
        return total;
    }

    private Long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private long parseLong(Object value) {
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
