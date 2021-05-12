package com.game.core.cache;

import com.game.core.cache.mapper.ValueConvertMapper;
import com.game.core.cache.mapper.ValueConverter;
import com.game.core.cache.mapper.mongodb.MongoDBConvertMapper;
import com.game.core.cache.mapper.redis.RedisConvertMapper;

public enum CacheType {
    MongoDb(true, new MongoDBConvertMapper()),
    Redis(false, new RedisConvertMapper()),
    ;

    private final boolean isDBType;
    private final ValueConvertMapper valueConvertMapper;

    CacheType(boolean isDBType, ValueConvertMapper valueConvertMapper) {
        this.isDBType = isDBType;
        this.valueConvertMapper = valueConvertMapper;
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
