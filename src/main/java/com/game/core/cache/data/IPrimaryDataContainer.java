package com.game.core.cache.data;

import com.game.common.lock.LockUtil;
import com.game.common.util.Holder;
import com.game.core.cache.exception.CacheException;

import java.util.Collection;
import java.util.List;

public interface IPrimaryDataContainer<K, V extends IData<K>> {

    long primaryKey();

    int count();

    V get(K secondaryKey);

    Collection<V> getAll();

    V replaceOne(V value);

    void replaceBatch(Collection<V> values);

    V removeOne(K secondaryKey);

    void removeBatch(Collection<K> secondaryKeys);
}
