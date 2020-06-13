package com.game.cache.dao;

import com.game.cache.data.IData;

import java.util.Collection;

public interface IDataMapDao<K, V extends IData<K>> {

    int count(long primaryKey);

    V get(long primaryKey, K secondaryKey);

    Collection<V> getAll(long primaryKey);

    V replaceOne(long primaryKey, V value);

    void replaceBatch(long primaryKey, Collection<V> values);

    V deleteOne(long primaryKey, K secondaryKey);

    void deleteBatch(long primaryKey, Collection<K> secondaryKeys);
}
