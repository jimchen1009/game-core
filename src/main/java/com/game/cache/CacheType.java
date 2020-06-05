package com.game.cache;

import com.game.cache.mapper.ValueConvertMapper;
import com.game.cache.mapper.mongodb.MongoDBConvertMapper;
import com.game.cache.mapper.redis.RedisConvertMapper;

public enum CacheType {
    MongoDb(new MongoDBConvertMapper()),
    Redis(new RedisConvertMapper()),
    ;

    private final ValueConvertMapper valueConvertMapper;

    CacheType(ValueConvertMapper valueConvertMapper) {
        this.valueConvertMapper = valueConvertMapper;
    }

    public ValueConvertMapper getValueConvertMapper() {
        return valueConvertMapper;
    }
}
