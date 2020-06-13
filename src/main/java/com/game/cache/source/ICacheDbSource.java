package com.game.cache.source;

import com.game.cache.data.IData;
import com.game.cache.source.executor.ICacheSource;

import java.util.List;
import java.util.Map;

public interface ICacheDbSource<K, V extends IData<K>> extends ICacheSource<K, V> {

    CacheCollection getPrimaryCollection(long primaryKey);

    Map<Integer, CacheCollection> getSharedCollections(long primaryKey, List<Integer> primarySharedIds);
}
