package com.game.core.cache.dao;

import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.data.IData;
import com.game.core.cache.data.map.DataMapContainer;

import java.util.Collection;
import java.util.function.Consumer;

class DataCacheMapDao<K, V extends IData<K>> implements IDataCacheMapDao<K, V> {

    private final DataMapContainer<K, V> mapContainer;

    public DataCacheMapDao(DataMapContainer<K, V> mapContainer) {
        this.mapContainer = mapContainer;
    }

    @Override
    public ICacheUniqueId getCacheUniqueId() {
        return mapContainer.getDataSource().getCacheUniqueId();
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
        return mapContainer.getNoCache(primaryKey, secondaryKey).getValue();
    }

    @Override
    public Collection<V> getAll(long primaryKey) {
        return mapContainer.getAll(primaryKey);
    }

    @Override
    public Collection<V> getAllNotCache(long primaryKey) {
        return mapContainer.getAllNoCache(primaryKey);
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
