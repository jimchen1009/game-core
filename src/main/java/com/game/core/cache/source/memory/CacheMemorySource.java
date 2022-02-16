package com.game.core.cache.source.memory;

import com.game.core.cache.CacheType;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.data.DataCollection;
import com.game.core.cache.data.IData;
import com.game.core.cache.key.IKeyValueBuilder;
import com.game.core.cache.source.CacheSource;
import com.game.core.cache.source.executor.ICacheExecutor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CacheMemorySource<K, V extends IData<K>> extends CacheSource<K, V> {

    public CacheMemorySource(ICacheUniqueId cacheUniqueId, IKeyValueBuilder<K> secondaryBuilder, ICacheExecutor executor) {
        super(cacheUniqueId, secondaryBuilder, executor);
    }

    @Override
    public V get(long primaryKey, K secondaryKey) {
        return null;
    }

    @Override
    public List<V> getAll(long primaryKey) {
        return Collections.emptyList();
    }

    @Override
    public DataCollection<K, V> getCollection(long primaryKey) {
        return new DataCollection<>(Collections.emptyList());
    }

    @Override
    public boolean replaceOne(long primaryKey, V data) {
        return true;
    }

    @Override
    public boolean replaceBatch(long primaryKey, Collection<V> dataList) {
        return true;
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.Memory;
    }

    @Override
    public boolean deleteOne(long primaryKey, K secondaryKey) {
        return true;
    }

    @Override
    public boolean deleteBatch(long primaryKey, Collection<K> secondaryKeys) {
        return true;
    }
}
