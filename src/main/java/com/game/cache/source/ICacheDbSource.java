package com.game.cache.source;

import com.game.cache.data.IData;
import com.game.cache.source.executor.ICacheSource;

import java.util.List;
import java.util.Map;

public interface ICacheDbSource<PK, K, V extends IData<K>> extends ICacheSource<PK, K, V> {

    CacheCollection getPrimaryCollection(PK primaryKey);

    Map<Integer, CacheCollection> getSharedCollections(PK primaryKey, List<Integer> primarySharedIds);
}
