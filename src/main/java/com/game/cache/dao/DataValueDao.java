package com.game.cache.dao;

import com.game.cache.data.DataSource;
import com.game.cache.data.IData;
import com.game.cache.exception.CacheException;
import com.game.cache.mapper.ValueConvertMapper;
import com.game.cache.source.executor.ICacheSource;
import com.game.common.log.LogUtil;

public class DataValueDao<PK, V extends IData<PK>> implements IDataValueDao<PK, V> {

    private final DataSource<PK, PK, V> dataSource;

    public DataValueDao(Class<V> aClass, ValueConvertMapper convertMapper, ICacheSource<PK, PK, V> cacheSource) {
        this.dataSource = new DataSource<>(aClass, convertMapper, cacheSource);
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
