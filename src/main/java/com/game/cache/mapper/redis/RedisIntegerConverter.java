package com.game.cache.mapper.redis;

import com.game.cache.mapper.ValueConverter;

public class RedisIntegerConverter extends ValueConverter<Integer> {

    public RedisIntegerConverter() {
        super(0, 0);
    }

    @Override
    protected Integer decode0(Object cacheValue) {
        return (Integer)cacheValue;
    }

    @Override
    protected Object encode0(Object dataValue) {
        return dataValue;
    }
}
