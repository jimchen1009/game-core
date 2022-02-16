package com.game.core.cache;

import com.game.core.cache.mapper.ValueConvertMapper;
import com.game.core.cache.mapper.mongodb.MongoDBConvertMapper;
import com.game.core.cache.mapper.mysql.MySQLConvertMapper;
import com.game.core.cache.mapper.redis.RedisConvertMapper;
import com.game.core.cache.source.executor.ICacheSource;
import com.game.core.cache.source.memory.CacheMemorySource;
import com.game.core.cache.source.mongodb.CacheMongoDbSource;
import com.game.core.cache.source.redis.CacheRedisSource;
import com.game.core.cache.source.sql.CacheSqlDbSource;

public enum CacheType {
    Memory(false, CacheMemorySource.class, new ValueConvertMapper()),
    Redis(false, CacheRedisSource.class, new RedisConvertMapper()),
    MongoDb(true, CacheMongoDbSource.class, new MongoDBConvertMapper()),
    MySQL(true, CacheSqlDbSource.class, new MySQLConvertMapper()),
    ;

    private final boolean isDB;
    private final Class<? extends ICacheSource> cacheClass;
    private final ValueConvertMapper valueConvertMapper;

    CacheType(boolean isDB, Class<? extends ICacheSource> cacheClass, ValueConvertMapper valueConvertMapper) {
        this.isDB = isDB;
        this.cacheClass = cacheClass;
        this.valueConvertMapper = valueConvertMapper;
    }

    public boolean isDB() {
        return isDB;
    }

    public Class<? extends ICacheSource> getCacheClass() {
        return cacheClass;
    }

    public ValueConvertMapper getConvertMapper() {
        return valueConvertMapper;
    }
}
