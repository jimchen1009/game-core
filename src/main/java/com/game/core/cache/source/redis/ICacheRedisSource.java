package com.game.core.cache.source.redis;

import com.game.core.cache.CacheInformation;
import com.game.core.cache.data.IData;
import com.game.core.cache.source.executor.ICacheSource;

import java.util.Collection;

public interface ICacheRedisSource<K, V extends IData<K>> extends ICacheSource<K, V> {

	boolean replaceBatch(long primaryKey, Collection<V> values, CacheInformation information);

}
