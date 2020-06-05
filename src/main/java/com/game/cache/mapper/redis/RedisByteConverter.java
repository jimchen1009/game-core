package com.game.cache.mapper.redis;

import com.game.cache.mapper.ValueConverter;

public class RedisByteConverter extends ValueConverter<Byte> {

    public RedisByteConverter() {
        super((byte)0, (byte)0);
    }

    @Override
    protected Byte decode0(Object cacheValue) {
        return Byte.parseByte(cacheValue.toString());
    }

    @Override
    protected Object encode0(Object dataValue) {
        return dataValue.toString();
    }
}
