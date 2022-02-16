package com.game.core.cache.dao;

import com.game.common.log.LogUtil;
import com.game.core.cache.data.IData;
import com.game.core.cache.data.IDataSource;
import com.game.core.cache.exception.CacheException;

class DataValueDao<V extends IData<Long>> implements IDataValueDao<V> {

    private final IDataSource<Long, V> dataSource;

    public DataValueDao(IDataSource<Long, V> dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public V get(long primaryKey) {
        V value = dataSource.get(primaryKey, primaryKey);
        return value == null || value.isDeleted() ? null : value;
    }

    @Override
    public V replace(V value) {
        boolean isSuccess = dataSource.replaceOne(value.secondaryKey(), value);
        if (isSuccess){
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
            throw new CacheException("remove error, %s", primaryKey);
        }
    }
}
