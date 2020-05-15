package com.game.cache.property;

import com.game.common.property.SystemPropertyKey;

public class CachePropertyKey extends SystemPropertyKey {

    public static final CachePropertyKey WRITE_BACK_BATCH_COUNT = new CachePropertyKey("cache.source.flush.batch_count", "5");
    public static final CachePropertyKey WRITE_BACK_EXPIRED_DURATION = new CachePropertyKey("cache.source.flush.expired_duration", "10");

    public CachePropertyKey(String property, String defaultValue) {
        super(property, defaultValue);
    }
}
