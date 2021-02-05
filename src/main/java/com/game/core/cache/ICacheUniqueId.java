package com.game.core.cache;

import com.game.core.cache.mapper.IClassAnnotation;

import java.util.List;
import java.util.Map;

public interface ICacheUniqueId extends IClassConfig, IClassAnnotation {

	String getSourceUniqueId();

	String getRedisKeyString(long primaryKey);

	List<Map.Entry<String, Object>> createPrimaryUniqueKeys(long primaryKey);
}
