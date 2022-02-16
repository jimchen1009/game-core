package com.game.core.cache;

public interface IClassConfig {

	<V> Class<V> getAClass();

	CacheType getCacheType();

	String getName();

	boolean isAccountCache();

	boolean isCacheLoadAdvance();

	boolean isRedisSupport();

	long getRedisDuration();

	boolean isDelayUpdate();

	int getVersionId();

	IClassConfig cloneConfig();
}
