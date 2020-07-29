package com.game.core.cache.mapper;

public class ShortValueConverter extends ValueConverter<Short> {

    public ShortValueConverter() {
        super((short)0, (short)0);
    }

    @Override
    protected Short decode0(Object cacheValue) {
        return (short)cacheValue;
    }

    @Override
    protected Object encode0(Object dataValue) {
        return dataValue;
    }
}
