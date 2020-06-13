package com.game.cache.dao;

import com.game.cache.data.IData;
import com.game.cache.data.IDataSource;
import com.game.cache.data.map.DataMapContainer;
import com.game.common.util.Holder;

import java.util.Collection;
import java.util.function.Consumer;

class DataCacheMapDao<K, V extends IData<K>> implements IDataCacheMapDao<K, V> {

    private final IDataSource< K, V> dataSource;
    private final DataMapContainer<K, V> mapContainer;

    public DataCacheMapDao(IDataSource<K, V> dataSource, DataMapContainer<K, V> mapContainer) {
        this.dataSource = dataSource;
        this.mapContainer = mapContainer;
    }

    @Override
    public boolean existCache(long primaryKey) {
        return mapContainer.existCache(primaryKey);
    }

    @Override
    public int count(long primaryKey) {
        return mapContainer.count(primaryKey);
    }

    @Override
    public V get(long primaryKey, K secondaryKey) {
        return mapContainer.get(primaryKey, secondaryKey);
    }

    @Override
    public V getNotCache(long primaryKey, K secondaryKey) {
        Holder<V> holder = mapContainer.getNoCache(primaryKey, secondaryKey);
        if (holder != null){
            return holder.getValue();
        }
        return dataSource.get(primaryKey, secondaryKey);
    }

    @Override
    public Collection<V> getAll(long primaryKey) {
        return mapContainer.getAll(primaryKey);
    }

    @Override
    public Collection<V> getAllNotCache(long primaryKey) {
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
    public void flushOne(long primaryKey, long currentTime, Consumer<Boolean> consumer) {
        mapContainer.flushOne(primaryKey, currentTime, consumer);
    }

    @Override
    public V replaceOne(long primaryKey, V value) {
        return mapContainer.replaceOne(primaryKey, value);
    }

    @Override
    public void replaceBatch(long primaryKey, Collection<V> values) {
        mapContainer.replaceBatch(primaryKey, values);
    }

    @Override
    public V deleteOne(long primaryKey, K secondaryKey) {
        return mapContainer.removeOne(primaryKey, secondaryKey);
    }

    @Override
    public void deleteBatch(long primaryKey, Collection<K> secondaryKeys) {
        mapContainer.removeBatch(primaryKey, secondaryKeys);
    }
}
