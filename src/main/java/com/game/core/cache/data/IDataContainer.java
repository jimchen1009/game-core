package com.game.core.cache.data;

import com.game.common.util.Holder;

import java.util.Collection;
import java.util.function.Consumer;

public interface IDataContainer<K, V extends IData<K>> {

    boolean existCache(long primaryKey);

    int count(long primaryKey);

    V get(long primaryKey, K secondaryKey);

    Holder<V> getNoCache(long primaryKey, K secondaryKey);

    Collection<V> getAll(long primaryKey);

    Collection<V> getAllNoCache(long primaryKey);

    V replaceOne(long primaryKey, V value);

    void replaceBatch(long primaryKey, Collection<V> values);

    V removeOne(long primaryKey, K secondaryKeys);

    void removeBatch(long primaryKey, Collection<K> secondaryKeys);

    boolean flushAll(long currentTime);

    void flushOne(long primaryKey, long currentTime, Consumer<Boolean> consumer);
}
