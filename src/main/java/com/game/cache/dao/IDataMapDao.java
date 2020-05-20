package com.game.cache.dao;

import com.game.cache.data.IData;

import java.util.Collection;

public interface IDataMapDao<PK, K, V extends IData<K>> {

    int count(PK primaryKey);

    V get(PK primaryKey, K secondaryKey);

    Collection<V> getAll(PK primaryKey);

    V replaceOne(PK primaryKey, V value);

    void replaceBatch(PK primaryKey, Collection<V> values);

    V deleteOne(PK primaryKey, K secondaryKey);

    void deleteBatch(PK primaryKey, Collection<K> secondaryKeys);
}
