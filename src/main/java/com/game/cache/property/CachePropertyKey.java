package com.game.cache.property;

import com.game.common.property.SystemPropertyKey;

public class CachePropertyKey extends SystemPropertyKey {

    public static final CachePropertyKey FLUSH_BATCH_COUNT = new CachePropertyKey("cache.source.flush.batch_count", "2000");
    public static final CachePropertyKey FLUSH_PRIMARY_TRY_COUNT = new CachePropertyKey("cache.source.flush.primary_try_count", "2");
    public static final CachePropertyKey FLUSH_EXPIRED_DURATION = new CachePropertyKey("cache.source.flush.expired_duration", "10");
    public static final CachePropertyKey FLUSH_TIME_OUT = new CachePropertyKey("cache.source.flush.time_out", "500");
    public static final CachePropertyKey CACHE_MAX_COUNT = new CachePropertyKey("cache.source.cache.max_count", "500");

    public CachePropertyKey(String property, String defaultValue) {
        super(property, defaultValue);
    }
}
