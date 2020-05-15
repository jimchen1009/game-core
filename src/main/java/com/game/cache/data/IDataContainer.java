package com.game.cache.data;

import java.util.Collection;

public interface IDataContainer<PK, K, V extends IData<K>> {

    int count(PK primaryKey);

    V get(PK primaryKey, K key);

    V get(PK primaryKey, K key, boolean isClone);

    Collection<V> getAll(PK primaryKey);

    Collection<V> getAll(PK primaryKey, boolean isClone);

    V replaceOne(PK primaryKey, V value);

    void replaceBatch(PK primaryKey, Collection<V> values);

    V removeOne(PK primaryKey, K key);

    void removeBatch(PK primaryKey, Collection<K> keys);
}
