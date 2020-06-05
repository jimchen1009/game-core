package com.game.cache.dao;

import com.game.cache.data.IData;

import java.util.Collection;

public interface IDataCacheMapDao<PK, K, V extends IData<K>> extends IDataMapDao<PK, K, V>{

    boolean existCache(PK primaryKey);

    V getNotCache(PK primaryKey, K secondaryKey);

    Collection<V> getAllNotCache(PK primaryKey);
}
