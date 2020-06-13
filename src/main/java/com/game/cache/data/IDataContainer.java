package com.game.cache.data;

import com.game.common.util.Holder;

import java.util.Collection;
import java.util.function.Consumer;

public interface IDataContainer<K, V extends IData<K>> {

    boolean existCache(long id);

    int count(long id);

    V get(long id, K secondaryKey);

    Holder<V> getNoCache(long id, K secondaryKey);

    Collection<V> getAll(long id);

    Collection<V> getAllNoCache(long id);

    V replaceOne(long id, V value);

    void replaceBatch(long id, Collection<V> values);

    V removeOne(long id, K secondaryKeys);

    void removeBatch(long id, Collection<K> secondaryKeys);

    boolean flushAll(long currentTime);

    void flushOne(long primaryKey, long currentTime, Consumer<Boolean> consumer);
}
