package com.game.cache.source.executor;

import com.game.cache.mapper.ClassConfig;
import com.game.cache.source.CacheCollection;
import com.game.cache.source.ICacheDelaySource;
import com.game.cache.source.ICacheKeyValueBuilder;
import com.game.cache.source.KeyCacheValue;
import com.game.common.lock.LockKey;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ICacheSource<PK, K, V> {

    LockKey getLockKey(PK primaryKey);

    Class<V> getAClass();

    ClassConfig getClassConfig();

    Map<String, Object> get(PK primaryKey, K secondaryKey);

    Collection<Map<String, Object>> getAll(PK primaryKey);

    CacheCollection getCollection(PK primaryKey);

    boolean replaceOne(PK primaryKey, KeyCacheValue<K> keyCacheValue);

    boolean replaceBatch(PK primaryKey, List<KeyCacheValue<K>> keyCacheValueList);

    boolean deleteOne(PK primaryKey, K secondaryKey);

    boolean deleteBatch(PK primaryKey, Collection<K> secondaryKeys);

    ICacheKeyValueBuilder<PK, K> getKeyValueBuilder();

    ICacheDelaySource<PK, K, V> createDelayUpdateSource(ICacheExecutor executor);
}
