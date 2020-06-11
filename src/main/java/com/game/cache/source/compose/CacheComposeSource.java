package com.game.cache.source.compose;

import com.game.cache.CacheType;
import com.game.cache.data.DataCollection;
import com.game.cache.data.IData;
import com.game.cache.mapper.ClassConfig;
import com.game.cache.mapper.IClassConverter;
import com.game.cache.source.ICacheDelaySource;
import com.game.cache.source.ICacheKeyValueBuilder;
import com.game.cache.source.executor.ICacheExecutor;
import com.game.cache.source.executor.ICacheSource;
import com.game.cache.source.redis.ICacheRedisSource;
import com.game.common.lock.LockKey;

import java.util.Collection;
import java.util.List;

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
    public V get(PK primaryKey, K secondaryKey) {
        V data = redisSource.get(primaryKey, secondaryKey);
        if (data == null){
            data = dbSource.get(primaryKey, secondaryKey);
        }
        return data;
    }

    @Override
    public List<V> getAll(PK primaryKey) {
        List<V> dataList = redisSource.getAll(primaryKey);
        if (dataList == null){
            dataList = dbSource.getAll(primaryKey);
        }
        return dataList;
    }

    @Override
    public DataCollection<K, V> getCollection(PK primaryKey) {
        DataCollection<K, V> collection = redisSource.getCollection(primaryKey);
        if (collection == null || collection.isEmpty()){
            collection = dbSource.getCollection(primaryKey);
        }
        return collection;
    }

    @Override
    public boolean replaceOne(PK primaryKey, V value) {
        boolean isSuccess = redisSource.replaceOne(primaryKey, value);
        if (isSuccess){
            isSuccess = dbSource.replaceOne(primaryKey, value);
        }
        return isSuccess;
    }

    @Override
    public boolean replaceBatch(PK primaryKey, Collection<V> values) {
        boolean isSuccess = redisSource.replaceBatch(primaryKey, values);
        if (isSuccess){
            isSuccess = dbSource.replaceBatch(primaryKey, values);
        }
        return isSuccess;
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
    public CacheType getCacheType() {
       throw new UnsupportedOperationException();
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
    public V cloneValue(V value) {
        return dbSource.cloneValue(value);
    }

    @Override
    public IClassConverter<K, V> getConverter() {
        return dbSource.getConverter();
    }

    @Override
    public boolean flushAll() {
        return dbSource.flushAll();
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
