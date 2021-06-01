package com.game.core.cache.source;

import com.game.core.cache.CacheKeyValue;

import java.util.List;

public interface ICacheKeyValueBuilder<K> {

    List<CacheKeyValue> createPrimaryKeyValue(long primaryKey);

    List<CacheKeyValue> createCombineUniqueKeyValue(long primaryKey, K secondaryKey);

    String toSecondaryKeyString(K secondaryKey);
}
