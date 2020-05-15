package com.game.cache.data;

import com.game.common.lock.LockKey;

import java.util.Collection;
import java.util.List;

public interface IDataSource<PK, K, V extends Data<K>> {

    LockKey getLockKey();

    V get(PK primaryKey, K key);

    List<V> getAll(PK primaryKey);

    DataCollection<K, V> getCollection(PK primaryKey);

    boolean replaceOne(PK primaryKey, V value);

    boolean replaceBatch(PK primaryKey, Collection<V> values);

    boolean deleteOne(PK primaryKey, K key);

    boolean deleteBatch(PK primaryKey, Collection<K> keys);

    V cloneValue(V value);}
