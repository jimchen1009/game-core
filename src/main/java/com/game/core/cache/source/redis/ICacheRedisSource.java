package com.game.core.cache.source.redis;

import com.game.core.cache.data.DataCollection;
import com.game.core.cache.data.IData;
import com.game.core.cache.source.executor.ICacheSource;

import java.util.Collection;

public interface ICacheRedisSource<K, V extends IData<K>> extends ICacheSource<K, V> {

	RedisCollection<K, V> getCollection(long primaryKey);

	boolean replaceBatch(long primaryKey, DataCollection<K, V> dataCollection);

	boolean replaceBatch(long primaryKey, Collection<K> secondaryKeys, Collection<V> dataList);
}
