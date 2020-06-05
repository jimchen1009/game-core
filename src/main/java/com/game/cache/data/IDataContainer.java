package com.game.cache.data;

import com.game.common.util.Holder;

import java.util.Collection;

public interface IDataContainer<PK, K, V extends IData<K>> {

    boolean existCache(PK primaryKey);

    int count(PK primaryKey);

    V get(PK primaryKey, K secondaryKey);

    Holder<V> getNoCache(PK primaryKey, K secondaryKey);

    Collection<V> getAll(PK primaryKey);

    Collection<V> getAllNoCache(PK primaryKey);

    V replaceOne(PK primaryKey, V value);

    void replaceBatch(PK primaryKey, Collection<V> values);

    V removeOne(PK primaryKey, K secondaryKeys);

    void removeBatch(PK primaryKey, Collection<K> secondaryKeys);
}
