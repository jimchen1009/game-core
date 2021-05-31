package com.game.core.cache.dao;

import com.game.common.log.LogUtil;
import com.game.core.cache.data.IData;
import com.game.core.cache.data.IDataSource;
import com.game.core.cache.exception.CacheException;

import java.util.Collection;

class DataMapDao<K, V extends IData<K>> implements IDataMapDao<K, V> {

    private final IDataSource<K, V> dataSource;

    public DataMapDao(IDataSource<K, V> dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int count(long primaryKey) {
        return dataSource.getAll(primaryKey).size();
    }

    @Override
    public V get(long primaryKey, K secondaryKey) {
        return dataSource.get(primaryKey, secondaryKey);
    }

    @Override
    public Collection<V> getAll(long primaryKey) {
        return dataSource.getAll(primaryKey);
    }


    @Override
    public V replaceOne(long primaryKey, V value) {
        boolean isSuccess = dataSource.replaceOne(primaryKey, value);
        if (isSuccess){
            value.clearCacheBitIndex();
            return null;
        }
        else {
            throw new CacheException("primaryKey:%s replaceOne error, %s", primaryKey, LogUtil.toJSONString(value));
        }
    }

    @Override
    public void replaceBatch(long primaryKey, Collection<V> values) {
        boolean isSuccess = dataSource.replaceBatch(primaryKey, values);
        if (isSuccess){
            values.forEach(V::clearCacheBitIndex);
        }
        else {
            throw new CacheException("primaryKey:%s replaceBatch error, %s", primaryKey, LogUtil.toJSONString(values));
        }
    }

    @Override
    public V deleteOne(long primaryKey, K secondaryKey) {
        boolean isSuccess = dataSource.deleteOne(primaryKey, secondaryKey);
        if (isSuccess){
            return null;
        }
        else {
            throw new CacheException("primaryKey:%s deleteOne error, %s", primaryKey, LogUtil.toJSONString(secondaryKey));
        }
    }

    @Override
    public void deleteBatch(long primaryKey, Collection<K> secondaryKeys) {
        boolean isSuccess = dataSource.deleteBatch(primaryKey, secondaryKeys);
        if (isSuccess){
        }
        else {
            throw new CacheException("primaryKey:%s deleteBatch error, %s", primaryKey, LogUtil.toJSONString(secondaryKeys));
        }
    }

}
