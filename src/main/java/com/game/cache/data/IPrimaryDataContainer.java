package com.game.cache.data;

import java.util.Collection;

public interface IPrimaryDataContainer<PK, K, V extends IData<K>> {

    PK primaryKey();

    int count();

    V get(K secondaryKey);

    Collection<V> getAll();

    V replaceOne(V value);

    void replaceBatch(Collection<V> values);

    V removeOne(K secondaryKey);

    void removeBatch(Collection<K> secondaryKeys);

    long getLatestUpdateTime();

    void updateLatestUpdateTime();
}
