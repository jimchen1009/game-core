package com.game.cache.dao;

import com.game.cache.data.DataSourceBuilder;
import com.game.cache.data.IData;
import com.game.cache.data.IDataSource;
import com.game.cache.exception.CacheException;
import com.game.cache.mapper.ValueConvertMapper;
import com.game.cache.source.executor.ICacheSource;
import com.game.common.log.LogUtil;

import java.util.Collection;

public class DataMapDao<PK, K, V extends IData<K>> implements IDataMapDao<PK, K, V> {

    private final IDataSource<PK, K, V> dataSource;

    public DataMapDao(Class<V> aClass, ValueConvertMapper convertMapper, ICacheSource<PK, K, V> cacheSource) {
        this.dataSource = DataSourceBuilder.newBuilder(aClass, cacheSource).setConvertMapper(convertMapper).build();
    }

    @Override
    public int count(PK primaryKey) {
        return dataSource.getAll(primaryKey).size();
    }

    @Override
    public V get(PK primaryKey, K secondaryKey) {
        return dataSource.get(primaryKey, secondaryKey);
    }

    @Override
    public Collection<V> getAll(PK primaryKey) {
        return dataSource.getAll(primaryKey);
    }

    @Override
    public V replaceOne(PK primaryKey, V value) {
        boolean isSuccess = dataSource.replaceOne(primaryKey, value);
        if (isSuccess){
            value.clearIndexChangedBits();
            return null;
        }
        else {
            throw new CacheException("primaryKey:%s replaceOne error, %s", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(value));
        }
    }

    @Override
    public void replaceBatch(PK primaryKey, Collection<V> values) {
        boolean isSuccess = dataSource.replaceBatch(primaryKey, values);
        if (isSuccess){
            values.forEach(V::clearIndexChangedBits);
        }
        else {
            throw new CacheException("primaryKey:%s replaceBatch error, %s", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(values));
        }
    }

    @Override
    public V deleteOne(PK primaryKey, K secondaryKey) {
        boolean isSuccess = dataSource.deleteOne(primaryKey, secondaryKey);
        if (isSuccess){
            return null;
        }
        else {
            throw new CacheException("primaryKey:%s deleteOne error, %s", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(secondaryKey));
        }
    }

    @Override
    public void deleteBatch(PK primaryKey, Collection<K> secondaryKeys) {
        boolean isSuccess = dataSource.deleteBatch(primaryKey, secondaryKeys);
        if (isSuccess){
        }
        else {
            throw new CacheException("primaryKey:%s deleteBatch error, %s", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(secondaryKeys));
        }
    }
}
