package com.game.cache.source.compose;

import com.game.cache.data.IData;
import com.game.cache.mapper.ClassConfig;
import com.game.cache.source.CacheCollection;
import com.game.cache.source.ICacheDelaySource;
import com.game.cache.source.ICacheKeyValueBuilder;
import com.game.cache.source.KeyCacheValue;
import com.game.cache.source.executor.ICacheExecutor;
import com.game.cache.source.executor.ICacheSource;
import com.game.cache.source.redis.ICacheRedisSource;
import com.game.common.lock.LockKey;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CacheComposeSource<PK, K, V extends IData<K>> implements ICacheSource<PK, K, V> {

    private final ICacheRedisSource<PK, K, V> redisSource;
    private final ICacheSource<PK, K, V> dbSource;

    public CacheComposeSource(ICacheRedisSource<PK, K, V> redisSource, ICacheSource<PK, K, V> dbSource) {
        this.redisSource = redisSource;
        this.dbSource = dbSource;
    }

    @Override
    public LockKey getLockKey(PK primaryKey) {
        return redisSource.getLockKey(primaryKey);
    }

    @Override
    public Class<V> getAClass() {
        return redisSource.getAClass();
    }

    @Override
    public ClassConfig getClassConfig() {
        return redisSource.getClassConfig();
    }

    @Override
    public Map<String, Object> get(PK primaryKey, K secondaryKey) {
        Map<String, Object> cacheValue = redisSource.get(primaryKey, secondaryKey);
        if (cacheValue == null){
            cacheValue = dbSource.get(primaryKey, secondaryKey);
        }
        return cacheValue;
    }

    @Override
    public Collection<Map<String, Object>> getAll(PK primaryKey) {
        Collection<Map<String, Object>> cacheValues = redisSource.getAll(primaryKey);
        if (cacheValues == null){
            cacheValues = dbSource.getAll(primaryKey);
        }
        return cacheValues;
    }

    @Override
    public CacheCollection getCollection(PK primaryKey) {
        CacheCollection collection = redisSource.getCollection(primaryKey);
        if (collection == null || collection.isEmpty()){
            collection = dbSource.getCollection(primaryKey);
        }
        return collection;
    }

    @Override
    public boolean replaceOne(PK primaryKey, KeyCacheValue<K> keyCacheValue) {
        boolean isSuccess = redisSource.replaceOne(primaryKey, keyCacheValue);
        if (isSuccess){
            isSuccess = dbSource.replaceOne(primaryKey, keyCacheValue);
        }
        return isSuccess;
    }

    @Override
    public boolean replaceBatch(PK primaryKey, List<KeyCacheValue<K>> keyCacheValues) {
        boolean isSuccess = redisSource.replaceBatch(primaryKey, keyCacheValues);
        if (isSuccess){
            isSuccess = dbSource.replaceBatch(primaryKey, keyCacheValues);
        }
        return isSuccess;
    }

    @Override
    public boolean deleteOne(PK primaryKey, K secondaryKey) {
        boolean isSuccess = redisSource.deleteOne(primaryKey, secondaryKey);
        if (isSuccess){
            isSuccess = dbSource.deleteOne(primaryKey, secondaryKey);
        }
        return isSuccess;
    }

    @Override
    public boolean deleteBatch(PK primaryKey, Collection<K> secondaryKeys) {
        boolean isSuccess = redisSource.deleteBatch(primaryKey, secondaryKeys);
        if (isSuccess){
            isSuccess = dbSource.deleteBatch(primaryKey, secondaryKeys);
        }
        return isSuccess;
    }

    @Override
    public ICacheKeyValueBuilder<PK, K> getKeyValueBuilder() {
        return redisSource.getKeyValueBuilder();
    }

    @Override
    public ICacheDelaySource<PK, K, V> createDelayUpdateSource(ICacheExecutor executor) {
        throw new UnsupportedOperationException();
    }
}
