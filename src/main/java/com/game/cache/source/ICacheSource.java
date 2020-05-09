package com.game.cache.source;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ICacheSource<PK, K> {

    Map<String, Object> get(PK primaryKey, K key);

    Collection<Map<String, Object>> getAll(PK primaryKey);

    CacheCollection getCollection(PK primaryKey);

    boolean replaceOne(PK primaryKey, Map<String, Object> cacheValue);

    boolean replaceBatch(PK primaryKey, List<Map<String, Object>> cacheValuesList);

    boolean deleteOne(PK primaryKey, K key);

    boolean deleteBatch(PK primaryKey, Collection<K> keys);
}
