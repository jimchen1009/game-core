package com.game.core.cache.source.executor;

import com.game.core.cache.CacheType;
import com.game.core.cache.data.IData;
import com.game.core.cache.data.IDataSource;
import com.game.core.cache.source.ICacheDelaySource;
import com.game.core.cache.source.ICacheKeyValueBuilder;

public interface ICacheSource<K, V extends IData<K>> extends IDataSource<K, V> {

    CacheType getCacheType();

    ICacheExecutor getExecutor();

    Class<V> getAClass();

    ICacheKeyValueBuilder<K> getKeyValueBuilder();

    ICacheDelaySource<K, V> createDelayUpdateSource();
}
