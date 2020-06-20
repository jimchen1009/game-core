package com.game.cache;

import com.game.cache.config.ClassConfig;

import java.util.List;

public interface ICacheUniqueKey {

	String getStringUniqueId();

	String getRedisKeyString(long primaryKey);

	<V> Class<V> getAClass();

	ClassConfig getClassConfig();

	String getName();

	int getPrimarySharedId();

	List<ICacheUniqueKey> sharedCacheDaoUniqueList();

	boolean isAccountCache();
}
