package com.game.core.cache.source;

import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.data.DataCollection;
import com.game.core.cache.data.IData;
import com.game.core.cache.key.IKeyValueBuilder;
import com.game.core.cache.source.executor.ICacheExecutor;

import java.util.List;

public abstract class CacheDbSource<K, V extends IData<K>> extends CacheSource<K, V> implements ICacheDbSource<K, V> {

    public CacheDbSource(ICacheUniqueId cacheUniqueId, IKeyValueBuilder<K> secondaryBuilder, ICacheExecutor executor) {
        super(cacheUniqueId, secondaryBuilder, executor);
    }

    @Override
    public DataCollection<K, V> getCollection(long primaryKey) {
        List<V> valueList = getAll(primaryKey);
        return new DataCollection<>(valueList);
    }
}
