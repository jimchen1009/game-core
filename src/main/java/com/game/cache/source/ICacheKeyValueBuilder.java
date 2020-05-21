package com.game.cache.source;

import com.game.cache.mapper.ClassInformation;

import java.util.Map;

public interface ICacheKeyValueBuilder<PK, K> {

    ClassInformation getClassInformation();

    Map<String, Object> createPrimaryKeyValue(PK primaryKey);

    Map<String, Object> createPrimarySecondaryKeyValue(PK primaryKey, K secondaryKey);

    Map<String, Object> createPrimarySecondaryKeyValue(Map<String, Object> cacheValue);

    PK createPrimaryKey(Map<String, Object> cacheValue);

    K createSecondaryKey(Map<String, Object> cacheValue);
}
