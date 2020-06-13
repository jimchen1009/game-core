package com.game.cache.dao;

import com.game.cache.data.IData;
import com.game.cache.data.IDataSource;
import com.game.cache.exception.CacheException;
import com.game.common.log.LogUtil;

class DataValueDao<V extends IData<Long>> implements IDataValueDao<V> {

    private final IDataSource<Long, V> dataSource;

    public DataValueDao(IDataSource<Long, V> dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public V get(long primaryKey) {
        return dataSource.get(primaryKey, primaryKey);
    }

    @Override
    public V getNotCache(long primaryKey) {
        return dataSource.get(primaryKey, primaryKey);
    }

    @Override
    public V replace(V value) {
        boolean isSuccess = dataSource.replaceOne(value.secondaryKey(), value);
        if (isSuccess){
            value.clearCacheBitIndex();
            return null;
        }
        else {
            throw new CacheException("replace error, %s", LogUtil.toJSONString(value));
        }
    }

    @Override
    public V delete(long primaryKey) {
        boolean isSuccess = dataSource.deleteOne(primaryKey, primaryKey);
        if (isSuccess){
            return null;
        }
        else {
            throw new CacheException("remove error, %s", LogUtil.toJSONString(primaryKey));
        }
    }
}
