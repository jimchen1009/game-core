package com.game.cache.mapper;

import com.game.cache.exception.CacheException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateValueConverter extends ValueConverter<Date> {

    private static final ThreadLocal<SimpleDateFormat> THREAD_LOCAL =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    public DateValueConverter() {
        super(null, null);
    }

    @Override
    protected Date decode0(Object cacheValue) {
        String string = (String) cacheValue;
        SimpleDateFormat dateFormat = THREAD_LOCAL.get();
        try {
            return dateFormat.parse(string);
        }
        catch (ParseException e) {
            throw new CacheException("%s", string, e);
        }
    }

    @Override
    protected Object encode0(Object dataValue) {
        SimpleDateFormat dateFormat = THREAD_LOCAL.get();
        return dateFormat.format((Date)dataValue);
    }
}
