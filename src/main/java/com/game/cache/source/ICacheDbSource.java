package com.game.cache.source;

import com.game.cache.data.IData;
import com.game.cache.source.executor.ICacheSource;
import com.game.cache.source.interact.CacheDBCollection;

import java.util.List;
import java.util.Map;

public interface ICacheDbSource<K, V extends IData<K>> extends ICacheSource<K, V> {

    CacheDBCollection getPrimaryCollection(long primaryKey);

    Map<Integer, CacheDBCollection> getSharedCollections(long primaryKey, List<Integer> primarySharedIds);
}
