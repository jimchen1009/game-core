package com.game.core.cache;

import com.game.core.cache.mapper.ValueConvertMapper;
import com.game.core.cache.mapper.ValueConverter;
import com.game.core.cache.mapper.mongodb.MongoDBConvertMapper;
import com.game.core.cache.mapper.redis.RedisConvertMapper;
import com.game.core.cache.source.executor.ICacheSource;
import com.game.core.cache.source.mongodb.CacheMongoDBSource;
import com.game.core.cache.source.redis.CacheRedisSource;

public enum CacheType {
    MongoDb(true, CacheMongoDBSource.class, new MongoDBConvertMapper()),
    Redis(false, CacheRedisSource.class, new RedisConvertMapper()),
    ;

    private final boolean isDBType;
    private final Class<? extends ICacheSource> cacheClass;
    private final ValueConvertMapper valueConvertMapper;

    CacheType(boolean isDBType, Class<? extends ICacheSource> cacheClass, ValueConvertMapper valueConvertMapper) {
        this.isDBType = isDBType;
        this.cacheClass = cacheClass;
        this.valueConvertMapper = valueConvertMapper;
    }

    public boolean isDBType() {
        return isDBType;
    }

    public Class<? extends ICacheSource> getCacheClass() {
        return cacheClass;
    }

    public ValueConvertMapper getConvertMapper() {
        return valueConvertMapper;
    }

    public void addValueConverter(ValueConverter<?> convert){
        valueConvertMapper.add(convert);
    }
}
