package com.game.core.cache.mapper;

public class BytesValueConverter extends ValueConverter<byte[]> {

    public BytesValueConverter() {
        super(null, null);
    }

    @Override
    protected byte[] decode0(Object cacheValue) {
        return (byte[])cacheValue;
    }

    @Override
    protected Object encode0(Object dataValue) {
        return dataValue;
    }
}
