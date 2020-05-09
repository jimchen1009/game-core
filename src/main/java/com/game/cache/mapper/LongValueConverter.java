package com.game.cache.mapper;

public class LongValueConverter extends ValueConverter<Long> {

    public LongValueConverter() {
        super(0L, 0L);
    }

    @Override
    protected Long decode0(Object cacheValue) {
        return (long)cacheValue;
    }

    @Override
    protected Object encode0(Object dataValue) {
        return dataValue;
    }
}
