package com.game.core.cache.data;

import com.game.common.lock.LockKey;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.mapper.IClassConverter;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public interface IDataSource<K, V extends IData<K>> {

    ICacheUniqueId getCacheUniqueId();

    LockKey getLockKey(long primaryKey);

    V get(long primaryKey, K secondaryKey);

    List<V> getAll(long primaryKey);

    DataCollection<K, V> getCollection(long primaryKey);

    boolean replaceOne(long primaryKey, V data);

    boolean replaceBatch(long primaryKey, Collection<V> dataList);

    boolean deleteOne(long primaryKey, K secondaryKey);

    boolean deleteBatch(long primaryKey, Collection<K> secondaryKeys);

    V cloneValue(V data);

    IClassConverter<K, V> getConverter();

    boolean flushAll(long currentTime);

    void flushOne(long primaryKey, long currentTime, Consumer<Boolean> consumer);
}