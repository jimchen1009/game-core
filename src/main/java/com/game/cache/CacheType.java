package com.game.cache;

import com.game.cache.mapper.ValueConvertMapper;
import com.game.cache.mapper.ValueConverter;
import com.game.cache.mapper.mongodb.MongoDBConvertMapper;
import com.game.cache.mapper.redis.RedisConvertMapper;

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
