package com.game.cache.mapper;

public class ByteValueConverter extends ValueConverter<Byte> {

    public ByteValueConverter() {
        super((byte)0, (byte)0);
    }

    @Override
    protected Byte decode0(Object cacheValue) {
        return (byte)cacheValue;
    }

    @Override
    protected Object encode0(Object dataValue) {
        return dataValue;
    }
}
