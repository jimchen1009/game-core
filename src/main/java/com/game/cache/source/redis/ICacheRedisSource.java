package com.game.cache.source.redis;

import com.game.cache.data.IData;
import com.game.cache.source.executor.ICacheSource;

public interface ICacheRedisSource<PK, K, V extends IData<K>> extends ICacheSource<PK, K, V> {
}
