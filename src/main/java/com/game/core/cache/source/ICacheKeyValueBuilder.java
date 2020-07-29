package com.game.core.cache.source;

import java.util.List;
import java.util.Map;

public interface ICacheKeyValueBuilder<K> {

    List<Map.Entry<String, Object>> createPrimaryKeyValue(long primaryKey);

    List<Map.Entry<String, Object>> createCombineUniqueKeyValue(long primaryKey, K secondaryKey);

    List<Map.Entry<String, Object>> createCombineUniqueKeyValue(Map<String, Object> cacheValue);

    K createSecondaryKey(Map<String, Object> cacheValue);

    String toSecondaryKeyString(K secondaryKey);

    String toSecondaryKeyString(Map<String, Object> cacheValue);

    K createSecondaryKey(String string);
}
