package com.game.cache.source.executor;

import com.game.cache.CacheType;
import com.game.cache.data.IData;
import com.game.cache.data.IDataSource;
import com.game.cache.mapper.ClassConfig;
import com.game.cache.source.ICacheDelaySource;
import com.game.cache.source.ICacheKeyValueBuilder;
import com.game.common.lock.LockKey;

import java.util.Collection;

public interface ICacheSource<PK, K, V extends IData<K>> extends IDataSource<PK, K, V> {

    LockKey getLockKey(PK primaryKey);

    Class<V> getAClass();

    ClassConfig getClassConfig();

    CacheType getCacheType();

    boolean deleteOne(PK primaryKey, K secondaryKey);

    boolean deleteBatch(PK primaryKey, Collection<K> secondaryKeys);

    ICacheKeyValueBuilder<PK, K> getKeyValueBuilder();

    ICacheDelaySource<PK, K, V> createDelayUpdateSource(ICacheExecutor executor);
}
