package com.game.cache.mapper.redis;

import com.game.cache.mapper.ValueConverter;

public class RedisLongConverter extends ValueConverter<Long> {

    public RedisLongConverter() {
        super(0L, 0L);
    }

    @Override
    protected Long decode0(Object cacheValue) {
        return Long.parseLong(cacheValue.toString());
    }

    @Override
    protected Object encode0(Object dataValue) {
        return dataValue.toString();
    }
}
