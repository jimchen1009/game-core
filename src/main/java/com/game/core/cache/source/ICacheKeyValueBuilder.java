package com.game.core.cache.source;

import java.util.List;
import java.util.Map;

public interface ICacheKeyValueBuilder<K> {

    List<Map.Entry<String, Object>> createPrimaryKeyValue(long primaryKey);

    List<Map.Entry<String, Object>> createCombineUniqueKeyValue(long primaryKey, K secondaryKey);

    String toSecondaryKeyString(K secondaryKey);
}
