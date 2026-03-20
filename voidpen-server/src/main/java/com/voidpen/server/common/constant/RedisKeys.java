package com.voidpen.server.common.constant;

public interface RedisKeys {

    String TOKEN_BLACKLIST = "voidpen:token:blacklist:";

    String BLOG_VIEWS = "voidpen:blog:views:";

    String BLOG_DETAIL = "voidpen:blog:detail:";

    String BLOG_LIKE = "voidpen:blog:like:";

    String SYSTEM_CONFIG = "voidpen:system:config:";

    String SYSTEM_CACHE_CLEAR_ALL_COOLDOWN = "voidpen:system:cache:clearall:cooldown";
}
