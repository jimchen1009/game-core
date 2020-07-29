package com.game.core.cache.mapper.redis;

import com.game.core.cache.mapper.ValueConverter;

public class RedisByteConverter extends ValueConverter<Byte> {

    public RedisByteConverter() {
        super((byte)0, (byte)0);
    }

    @Override
    protected Byte decode0(Object cacheValue) {
        return ((Integer)cacheValue).byteValue();
    }

    @Override
    protected Object encode0(Object dataValue) {
        return (int) (Byte) dataValue;
    }
}
