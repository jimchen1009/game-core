package com.game.cache.dao;

import com.game.cache.data.IData;
import com.game.cache.data.IDataSource;
import com.game.cache.data.value.IDataValueContainer;
import com.game.common.util.Holder;

class DataCacheValueDao<PK, V extends IData<PK>> implements IDataCacheValueDao<PK, V> {

    private final IDataSource<PK, PK, V> dataSource;
    private final IDataValueContainer<PK, V> valueContainer;

    public DataCacheValueDao(IDataSource<PK, PK, V> dataSource, IDataValueContainer<PK, V> valueContainer) {
        this.dataSource = dataSource;
        this.valueContainer = valueContainer;
    }

    @Override
    public V get(PK primaryKey) {
        return valueContainer.get(primaryKey);
    }

    @Override
    public boolean existCache(PK primaryKey) {
        return valueContainer.existCache(primaryKey);
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
    public boolean flushAll(long currentTime) {
        return valueContainer.flushAll(currentTime);
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
