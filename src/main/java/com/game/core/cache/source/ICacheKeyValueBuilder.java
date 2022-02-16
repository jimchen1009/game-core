package com.game.core.cache.source;

import com.game.core.cache.CacheKeyValue;

import java.util.List;
import java.util.Objects;

public interface ICacheKeyValueBuilder<K> {

    String toSecondaryKeyString(K secondaryKey);

    List<Object> createSecondaryValueList(K secondaryKey);

    List<Object> createCombineValueList(long primaryKey, K secondaryKey);

    List<CacheKeyValue> createPrimaryKeyValue(Long primaryKey);

    List<CacheKeyValue> createCombineKeyValue(Long primaryKey, K secondaryKey);
}
