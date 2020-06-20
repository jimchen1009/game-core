package com.game.cache;

import com.game.cache.mapper.ValueConvertMapper;
import com.game.cache.mapper.ValueConverter;
import com.game.cache.mapper.mongodb.MongoDBConvertMapper;
import com.game.cache.mapper.redis.RedisConvertMapper;

public enum CacheType {
    MongoDb(true, false, new MongoDBConvertMapper()),
    Redis(true, true, new RedisConvertMapper()),
    ;

    private final boolean fullCache;
    private final boolean cacheInternal;
    private final ValueConvertMapper valueConvertMapper;

    CacheType(boolean fullCache, boolean cacheInternal, ValueConvertMapper valueConvertMapper) {
        this.fullCache = fullCache;
        this.cacheInternal = cacheInternal;
        this.valueConvertMapper = valueConvertMapper;
    }

    public boolean isFullCache() {
        return fullCache;
    }

    public boolean isCacheInternal() {
        return cacheInternal;
    }

    public ValueConvertMapper getConvertMapper() {
        return valueConvertMapper;
    }

    public void addValueConverter(ValueConverter<?> convert){
        valueConvertMapper.add(convert);
    }
}
