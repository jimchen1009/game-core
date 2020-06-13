package com.game.cache.source.executor;

import com.game.cache.CacheType;
import com.game.cache.ICacheDaoUnique;
import com.game.cache.data.IData;
import com.game.cache.data.IDataSource;
import com.game.cache.source.ICacheDelaySource;
import com.game.cache.source.ICacheKeyValueBuilder;
import com.game.common.lock.LockKey;

import java.util.Collection;

public interface ICacheSource<K, V extends IData<K>> extends IDataSource<K, V> {

    LockKey getLockKey(long primaryKey);

    Class<V> getAClass();

    ICacheDaoUnique getCacheDaoUnique();

    CacheType getCacheType();

    boolean deleteOne(long primaryKey, K secondaryKey);

    boolean deleteBatch(long primaryKey, Collection<K> secondaryKeys);

    ICacheKeyValueBuilder<K> getKeyValueBuilder();

    ICacheDelaySource<K, V> createDelayUpdateSource(ICacheExecutor executor);
}
