package com.game.cache.mapper.redis;

import com.game.cache.mapper.ValueConverter;

public class RedisShortConverter extends ValueConverter<Short> {

    public RedisShortConverter() {
        super((short)0, (short)0);
    }

    @Override
    protected Short decode0(Object cacheValue) {
        return ((Integer)cacheValue).shortValue();
    }

    @Override
    protected Object encode0(Object dataValue) {
        return (int) (Short) dataValue;
    }
}
