package com.game.cache.data;

import com.game.cache.mapper.IClassConverter;
import com.game.common.lock.LockKey;

import java.util.Collection;
import java.util.List;

public interface IDataSource<PK, K, V extends IData<K>> {

    LockKey getLockKey();

    V get(PK primaryKey, K secondaryKey);

    List<V> getAll(PK primaryKey);

    DataCollection<K, V> getCollection(PK primaryKey);

    boolean replaceOne(PK primaryKey, V value);

    boolean replaceBatch(PK primaryKey, Collection<V> values);

    boolean deleteOne(PK primaryKey, K secondaryKey);

    boolean deleteBatch(PK primaryKey, Collection<K> secondaryKeys);

    V cloneValue(V value);

    IClassConverter<K, V> getConverter();
}