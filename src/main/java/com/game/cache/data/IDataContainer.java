package com.game.cache.data;

import com.game.cache.data.IData;

import java.util.Collection;

public interface IDataContainer<PK, K, V extends IData<K>> {

    PK primaryKey();

    int count();

    V get(K key);

    V get(K key, boolean isClone);

    Collection<V> getAll();

    Collection<V> getAll(boolean isClone);

    V replaceOne(V value);

    void replaceBatch(Collection<V> values);

    V removeOne(K key);

    void removeBatch(Collection<K> keys);
}
