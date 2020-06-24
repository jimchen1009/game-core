package com.game.cache.data;

import com.game.cache.CacheInformation;
import com.game.cache.ICacheUniqueId;
import com.game.cache.mapper.IClassConverter;
import com.game.cache.source.executor.ICacheSource;
import com.game.common.lock.LockKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

class DataSource<K, V extends IData<K>> implements IDataSource<K, V>{

    private static final Logger logger = LoggerFactory.getLogger(DataSource.class);

    private final ICacheSource<K, V> cacheSource;

    DataSource(ICacheSource<K, V> cacheSource) {
        this.cacheSource = cacheSource;
    }

    @Override
    public ICacheUniqueId getCacheUniqueId() {
        return cacheSource.getCacheUniqueId();
    }

    @Override
    public LockKey getLockKey(long primaryKey) {
        return cacheSource.getLockKey(primaryKey);
    }

    @Override
    public V get(long primaryKey, K secondaryKey) {
        V data = cacheSource.get(primaryKey, secondaryKey);
        return data ;
    }

    @Override
    public List<V> getAll(long primaryKey) {
        List<V> dataList = cacheSource.getAll(primaryKey);
        return dataList;
    }

    @Override
    public boolean replaceOne(long primaryKey, V value) {
        boolean isSuccess = cacheSource.replaceOne(primaryKey, value);
        if (isSuccess){

        }
        return isSuccess;
    }

    @Override
    public boolean replaceBatch(long primaryKey, Collection<V> values) {
        boolean isSuccess = cacheSource.replaceBatch(primaryKey, values);
        if (isSuccess){
        }
        return isSuccess;
    }

    @Override
    public boolean deleteOne(long primaryKey, K secondaryKey) {
        return cacheSource.deleteOne(primaryKey, secondaryKey);
    }

    @Override
    public boolean deleteBatch(long primaryKey, Collection<K> secondaryKeys) {
        return cacheSource.deleteBatch(primaryKey, secondaryKeys);
    }

    @Override
    public DataCollection<K, V> getCollection(long primaryKey) {
        DataCollection<K, V> collection = cacheSource.getCollection(primaryKey);
        return collection;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V cloneValue(V value) {
        return cacheSource.cloneValue(value);
    }

    @Override
    public IClassConverter<K, V> getConverter() {
        return cacheSource.getConverter();
    }

    @Override
    public boolean flushAll(long currentTime) {
        return cacheSource.flushAll(currentTime);
    }

    @Override
    public void flushOne(long primaryKey, long currentTime, Consumer<Boolean> consumer) {
        cacheSource.flushOne(primaryKey, currentTime, consumer);
    }

    @Override
    public boolean updateCacheInformation(long primaryKey, CacheInformation cacheInformation) {
        return cacheSource.updateCacheInformation(primaryKey, cacheInformation);
    }
}
