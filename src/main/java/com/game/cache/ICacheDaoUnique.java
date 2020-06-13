package com.game.cache;

import com.game.cache.mapper.ClassConfig;

import java.util.List;

public interface ICacheDaoUnique {

	String getDaoUniqueId();

	String getRedisKeyString(long primaryKey);

	<V> Class<V> getAClass();

	ClassConfig getClassConfig();

	String getTableName();

	int getPrimarySharedId();

	List<ICacheDaoUnique> sharedCacheDaoUniqueList();

	boolean isUserCache();
}
