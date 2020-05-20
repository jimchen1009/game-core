package com.game.cache;

import com.game.cache.mapper.ValueConvertMapper;
import com.game.cache.mapper.mongodb.MongoDBConvertMapper;

public enum CacheType {
    MongoDb(new MongoDBConvertMapper()),
    ;

    private final ValueConvertMapper valueConvertMapper;

    CacheType(ValueConvertMapper valueConvertMapper) {
        this.valueConvertMapper = valueConvertMapper;
    }

    public ValueConvertMapper getValueConvertMapper() {
        return valueConvertMapper;
    }
}
