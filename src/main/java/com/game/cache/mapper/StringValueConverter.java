package com.game.cache.mapper;

public class StringValueConverter extends ValueConverter<String> {

    public StringValueConverter() {
        super(null, null);
    }

    @Override
    protected String decode0(Object cacheValue) {
        return (String) cacheValue;
    }

    @Override
    protected Object encode0(Object dataValue) {
        return dataValue;
    }
}
