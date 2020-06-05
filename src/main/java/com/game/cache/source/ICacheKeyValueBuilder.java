package com.game.cache.source;

import com.game.cache.mapper.ClassInformation;

import java.util.List;
import java.util.Map;

public interface ICacheKeyValueBuilder<PK, K> {

    ClassInformation getClassInformation();

    List<Map.Entry<String, Object>> createPrimaryKeyValue(PK primaryKey);

    List<Map.Entry<String, Object>> createAllKeyValue(PK primaryKey, K secondaryKey);

    List<Map.Entry<String, Object>> createAllKeyValue(Map<String, Object> cacheValue);

    PK createPrimaryKey(Map<String, Object> cacheValue);

    K createSecondaryKey(Map<String, Object> cacheValue);

    String toSecondaryKeyString(Map<String, Object> cacheValue);

    Object[] toPrimaryKeyValue(PK primaryKey);

    String toPrimaryKeyString(PK primaryKey);

    String toSecondaryKeyString(K primaryKey);

    K createSecondaryKey(String string);
}
