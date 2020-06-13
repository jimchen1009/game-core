package com.game.cache.dao;

import com.game.cache.data.IData;

import java.util.Collection;

public interface IDataCacheMapDao<K, V extends IData<K>> extends IDataMapDao<K, V>, IDataCacheDao{

    V getNotCache(long primaryKey, K secondaryKey);

    Collection<V> getAllNotCache(long primaryKey);
}
