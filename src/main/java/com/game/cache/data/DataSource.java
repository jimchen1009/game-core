package com.game.cache.data;

import com.game.cache.mapper.ClassInformation;
import com.game.cache.mapper.IClassConverter;
import com.game.cache.source.executor.ICacheSource;
import com.game.common.lock.LockKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

class DataSource<PK, K, V extends IData<K>> implements IDataSource<PK, K, V>{

    private static final Logger logger = LoggerFactory.getLogger(DataSource.class);

    private final ICacheSource<PK, K, V> cacheSource;

    DataSource(ICacheSource<PK, K, V> cacheSource) {
        this.cacheSource = cacheSource;
    }

    @Override
    public LockKey getLockKey(PK primaryKey) {
        return cacheSource.getLockKey(primaryKey);
    }

    @Override
    public V get(PK primaryKey, K secondaryKey) {
        V data = cacheSource.get(primaryKey, secondaryKey);
        return data == null ? null : markValueSource(data);
    }

    @Override
    public List<V> getAll(PK primaryKey) {
        List<V> dataList = cacheSource.getAll(primaryKey);
        for (V data : dataList) {
            markValueSource(data);
        }
        return dataList;
    }

    @Override
    public boolean replaceOne(PK primaryKey, V value) {
        boolean isSuccess = cacheSource.replaceOne(primaryKey, value);
        if (isSuccess){
        }
        return isSuccess;
    }

    @Override
    public boolean replaceBatch(PK primaryKey, Collection<V> values) {
        boolean isSuccess = cacheSource.replaceBatch(primaryKey, values);
        if (isSuccess){
        }
        return isSuccess;
    }

    @Override
    public boolean deleteOne(PK primaryKey, K secondaryKey) {
        return cacheSource.deleteOne(primaryKey, secondaryKey);
    }

    @Override
    public boolean deleteBatch(PK primaryKey, Collection<K> secondaryKeys) {
        return cacheSource.deleteBatch(primaryKey, secondaryKeys);
    }

    @Override
    public DataCollection<K, V> getCollection(PK primaryKey) {
        DataCollection<K, V> collection = cacheSource.getCollection(primaryKey);
        for (V data : collection.getDataList()) {
            markValueSource(data);
        }
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
    public boolean flushAll() {
        return cacheSource.flushAll();
    }

    private V markValueSource(V dataValue){
        ClassInformation information = getConverter().getInformation();
        information.invokeSetBitIndex(dataValue, DataBitIndex.CacheCreated);
        return dataValue;
    }
}
