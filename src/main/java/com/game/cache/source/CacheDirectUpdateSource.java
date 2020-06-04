package com.game.cache.source;

import com.game.cache.data.IData;
import com.game.cache.key.IKeyValueBuilder;

import java.util.Collection;
import java.util.List;

public abstract class CacheDirectUpdateSource<PK, K, V extends IData<K>> extends CacheSource<PK, K, V> {

    public CacheDirectUpdateSource(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder, ICacheSourceInteract<PK> sourceInteract) {
        super(aClass, primaryBuilder, secondaryBuilder, sourceInteract);
    }

    @Override
    public boolean replaceOne(PK primaryKey, KeyCacheValue<K> keyCacheValue) {
        return replaceOne0(primaryKey, keyCacheValue);
    }

    protected abstract boolean replaceOne0(PK primaryKey, KeyCacheValue<K> keyCacheValue);

    @Override
    public boolean replaceBatch(PK primaryKey, List<KeyCacheValue<K>> keyCacheValueList) {
        if (keyCacheValueList.isEmpty()){
            return true;
        }
        return replaceBatch0(primaryKey, keyCacheValueList);
    }

    protected abstract boolean replaceBatch0(PK primaryKey, List<KeyCacheValue<K>> keyCacheValueList);

    @Override
    public boolean deleteOne(PK primaryKey, K secondaryKey) {
        return deleteOne0(primaryKey, secondaryKey);
    }

    protected abstract boolean deleteOne0(PK primaryKey, K secondaryKey);

    @Override
    public boolean deleteBatch(PK primaryKey, Collection<K> secondaryKeys) {
        if (secondaryKeys.isEmpty()){
            return true;
        }
        return deleteBatch0(primaryKey, secondaryKeys);
    }

    protected abstract boolean deleteBatch0(PK primaryKey, Collection<K> secondaryKeys);
}
