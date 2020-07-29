package com.game.core.cache.source;

import com.game.core.cache.CacheInformation;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.data.IData;
import com.game.core.cache.key.IKeyValueBuilder;
import com.game.core.cache.mapper.ClassConverter;
import com.game.core.cache.mapper.IClassConverter;
import com.game.core.cache.source.executor.ICacheSource;
import com.game.common.lock.LockKey;

import java.util.Map;
import java.util.function.Consumer;

public abstract class CacheSource<K, V extends IData<K>> implements ICacheSource<K, V> {

    private static final Object[] EMPTY = new Object[0];

    private final ICacheUniqueId cacheUniqueId;
    private final LockKey lockKey;
    protected final CacheKeyValueBuilder<K> keyValueBuilder;
    protected final IClassConverter<K, V> converter;

    public CacheSource(ICacheUniqueId cacheUniqueId, IKeyValueBuilder<K> secondaryBuilder) {
        this.cacheUniqueId = cacheUniqueId;
        Class<V> aClass = cacheUniqueId.getAClass();
        this.lockKey = LockKey.systemLockKey("cache").createLockKey(aClass.getSimpleName());
        this.keyValueBuilder = new CacheKeyValueBuilder<>(cacheUniqueId, secondaryBuilder);
        this.converter = new ClassConverter<>(aClass, cacheUniqueId, getCacheType());
    }

    @Override
    public LockKey getLockKey(long primaryKey) {
        return lockKey.createLockKey(String.valueOf(primaryKey));
    }

    @SuppressWarnings("unchecked")
    @Override
    public V cloneValue(V value) {
        return (V)value.clone(()-> convertClone(value));
    }

    @Override
    public IClassConverter<K, V> getConverter() {
        return converter;
    }

    @Override
    public Class<V> getAClass() {
        return cacheUniqueId.getAClass();
    }

    @Override
    public ICacheUniqueId getCacheUniqueId() {
        return cacheUniqueId;
    }

    @Override
    public ICacheKeyValueBuilder<K> getKeyValueBuilder() {
        return keyValueBuilder;
    }

    private V convertClone(V value){
        Map<String, Object> cacheValue = converter.convert2Cache(value);
        return converter.convert2Value(cacheValue);
    }

    @Override
    public boolean flushAll(long currentTime) {
        return true;
    }

    @Override
    public void flushOne(long primaryKey, long currentTime, Consumer<Boolean> consumer) {
        consumer.accept(true);
    }

    @Override
    public boolean updateCacheInformation(long primaryKey, CacheInformation cacheInformation) {
        return true;
    }
}
