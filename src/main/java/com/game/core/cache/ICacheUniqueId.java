package com.game.core.cache;

import com.game.core.cache.mapper.IClassAnnotation;

import java.util.List;

public interface ICacheUniqueId extends IClassConfig, IClassAnnotation {

	String getRedisKeyString(long primaryKey);

	List<CacheKeyValue> createPrimaryAndAdditionalKeys(long primaryKey);
}
