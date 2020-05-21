package com.game.cache.dao;

import com.game.cache.data.IData;
import com.game.cache.data.IDataSource;
import com.game.cache.exception.CacheException;
import com.game.common.log.LogUtil;

class DataValueDao<PK, V extends IData<PK>> implements IDataValueDao<PK, V> {

    private final IDataSource<PK, PK, V> dataSource;

    public DataValueDao(IDataSource<PK, PK, V> dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public V get(PK primaryKey) {
        return dataSource.get(primaryKey, primaryKey);
    }

    @Override
    public V getNotCache(PK primaryKey) {
        return dataSource.get(primaryKey, primaryKey);
    }

    @Override
    public V replace(V value) {
        boolean isSuccess = dataSource.replaceOne(value.secondaryKey(), value);
        if (isSuccess){
            value.clearIndexChangedBits();
            return null;
        }
        else {
            throw new CacheException("replace error, %s", LogUtil.toJSONString(value));
        }
    }

    @Override
    public V delete(PK primaryKey) {
        boolean isSuccess = dataSource.deleteOne(primaryKey, primaryKey);
        if (isSuccess){
            return null;
        }
        else {
            throw new CacheException("remove error, %s", LogUtil.toJSONString(primaryKey));
        }
    }
}
