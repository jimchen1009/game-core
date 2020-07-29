package com.game.core.cache;

import com.game.core.cache.mapper.ValueConvertMapper;
import com.game.core.cache.mapper.ValueConverter;
import com.game.core.cache.mapper.mongodb.MongoDBConvertMapper;
import com.game.core.cache.mapper.redis.RedisConvertMapper;

public enum CacheType {
    MongoDb(true, true, new MongoDBConvertMapper()),
    Redis(true, false, new RedisConvertMapper()),
    ;

    private final boolean fullCache;
    private final boolean isDBType;
    private final ValueConvertMapper valueConvertMapper;

    CacheType(boolean fullCache, boolean isDBType, ValueConvertMapper valueConvertMapper) {
        this.fullCache = fullCache;
        this.isDBType = isDBType;
        this.valueConvertMapper = valueConvertMapper;
    }

    public boolean isFullCache() {
        return fullCache;
    }

    public boolean isDBType() {
        return isDBType;
    }

    public ValueConvertMapper getConvertMapper() {
        return valueConvertMapper;
    }

    public void addValueConverter(ValueConverter<?> convert){
        valueConvertMapper.add(convert);
    }
}
