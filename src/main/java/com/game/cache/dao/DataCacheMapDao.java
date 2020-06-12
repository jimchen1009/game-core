package com.game.cache.dao;

import com.game.cache.data.IData;
import com.game.cache.data.IDataSource;
import com.game.cache.data.map.DataMapContainer;
import com.game.common.util.Holder;

import java.util.Collection;

class DataCacheMapDao<PK, K, V extends IData<K>> implements IDataCacheMapDao<PK, K, V> {

    private final IDataSource<PK, K, V> dataSource;
    private final DataMapContainer<PK, K, V> mapContainer;

    public DataCacheMapDao(IDataSource<PK, K, V> dataSource, DataMapContainer<PK, K, V> mapContainer) {
        this.dataSource = dataSource;
        this.mapContainer = mapContainer;
    }

    @Override
    public boolean existCache(PK primaryKey) {
        return mapContainer.existCache(primaryKey);
    }

    @Override
    public int count(PK primaryKey) {
        return mapContainer.count(primaryKey);
    }

    @Override
    public V get(PK primaryKey, K secondaryKey) {
        return mapContainer.get(primaryKey, secondaryKey);
    }

    @Override
    public V getNotCache(PK primaryKey, K secondaryKey) {
        Holder<V> holder = mapContainer.getNoCache(primaryKey, secondaryKey);
        if (holder != null){
            return holder.getValue();
        }
        return dataSource.get(primaryKey, secondaryKey);
    }

    @Override
    public Collection<V> getAll(PK primaryKey) {
        return mapContainer.getAll(primaryKey);
    }

    @Override
    public Collection<V> getAllNotCache(PK primaryKey) {
        Collection<V> values = mapContainer.getAllNoCache(primaryKey);
        if (values == null){
            values = dataSource.getAll(primaryKey);
        }
        return values;
    }

    @Override
    public boolean flushAll(long currentTime) {
        return mapContainer.flushAll(currentTime);
    }

    @Override
    public V replaceOne(PK primaryKey, V value) {
        return mapContainer.replaceOne(primaryKey, value);
    }

    @Override
    public void replaceBatch(PK primaryKey, Collection<V> values) {
        mapContainer.replaceBatch(primaryKey, values);
    }

    @Override
    public V deleteOne(PK primaryKey, K secondaryKey) {
        return mapContainer.removeOne(primaryKey, secondaryKey);
    }

    @Override
    public void deleteBatch(PK primaryKey, Collection<K> secondaryKeys) {
        mapContainer.removeBatch(primaryKey, secondaryKeys);
    }
}
