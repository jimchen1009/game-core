package com.game.core.cache.mapper;

public class IntegerValueConverter extends ValueConverter<Integer> {

    public IntegerValueConverter() {
        super(0, 0);
    }

    @Override
    protected Integer decode0(Object cacheValue) {
        return (int)cacheValue;
    }

    @Override
    protected Object encode0(Object dataValue) {
        return dataValue;
    }
}
