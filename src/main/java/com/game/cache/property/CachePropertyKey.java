package com.game.cache.property;

import com.game.common.property.SystemPropertyKey;

public class CachePropertyKey extends SystemPropertyKey {

    public static final CachePropertyKey KEY_SEPARATOR = new CachePropertyKey("com.game.cache.key.separator", ".");

    public CachePropertyKey(String property, String defaultValue) {
        super(property, defaultValue);
    }
}
