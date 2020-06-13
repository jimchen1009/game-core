package com.game.cache.source.redis;

import com.game.cache.CacheInformation;
import com.game.cache.data.IData;
import com.game.cache.source.executor.ICacheSource;

import java.util.Collection;

public interface ICacheRedisSource<K, V extends IData<K>> extends ICacheSource<K, V> {

	boolean replaceBatch(long primaryKey, Collection<V> values, CacheInformation information);

}
