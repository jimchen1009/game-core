package com.game.cache.dao;

import com.game.cache.data.DataSource;
import com.game.cache.data.IData;
import com.game.cache.data.IDataSource;
import com.game.cache.data.value.DataValueContainer;
import com.game.cache.data.value.IDataValueContainer;
import com.game.cache.mapper.ValueConvertMapper;
import com.game.cache.source.ICacheDelayUpdateSource;
import com.game.cache.source.executor.ICacheSource;
import com.game.common.util.Holder;

public class DataCacheValueDao<PK, V extends IData<PK>> implements IDataCacheValueDao<PK, V> {

    private final IDataSource<PK, PK, V> dataSource;
    private final IDataValueContainer<PK, V> valueContainer;

    public DataCacheValueDao(Class<V> aClass, ValueConvertMapper convertMapper, ICacheSource<PK, PK, V> cacheSource) {
        DataSource<PK, PK, V> dataSource = new DataSource<>(aClass, convertMapper, cacheSource);
        if (cacheSource instanceof ICacheDelayUpdateSource){
            @SuppressWarnings("unchecked") ICacheSource<PK, PK, V> cacheSource1 = ((ICacheDelayUpdateSource<PK, PK, V>) cacheSource).getCacheSource();
            this.dataSource = new DataSource<>(aClass, convertMapper, cacheSource1);
        }
        else {
            this.dataSource = new DataSource<>(aClass, convertMapper, cacheSource);
        }

        this.valueContainer = new DataValueContainer<>(dataSource);
    }

    @Override
    public V get(PK primaryKey) {
        return valueContainer.get(primaryKey);
    }

    @Override
    public V getNotCache(PK primaryKey) {
        Holder<V> holder = valueContainer.getNoCache(primaryKey);
        if (holder != null){
            return holder.getValue();
        }
        return dataSource.get(primaryKey, primaryKey);
    }

    @Override
    public V replace(V value) {
        return valueContainer.replace(value);
    }

    @Override
    public V delete(PK primaryKey) {
        return valueContainer.remove(primaryKey);
    }
}
