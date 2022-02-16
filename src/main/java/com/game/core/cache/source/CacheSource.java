package com.game.core.cache.source;

import com.game.common.lock.LockKey;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.data.IData;
import com.game.core.cache.key.IKeyValueBuilder;
import com.game.core.cache.mapper.ClassConverter;
import com.game.core.cache.mapper.IClassConverter;
import com.game.core.cache.source.executor.ICacheExecutor;
import com.game.core.cache.source.executor.ICacheSource;

import java.util.Map;
import java.util.function.Consumer;

public abstract class CacheSource<K, V extends IData<K>> implements ICacheSource<K, V> {

    private final ICacheUniqueId cacheUniqueId;
    private final LockKey lockKey;
    protected final CacheKeyValueBuilder<K> keyValueBuilder;
    protected final IClassConverter<K, V> converter;
    private final ICacheExecutor executor;

    public CacheSource(ICacheUniqueId cacheUniqueId, IKeyValueBuilder<K> secondaryBuilder, ICacheExecutor executor) {
        this.cacheUniqueId = cacheUniqueId;
        this.lockKey = LockKey.systemLockKey("cache").createLockKey(cacheUniqueId.getName());
        this.keyValueBuilder = new CacheKeyValueBuilder<>(cacheUniqueId, secondaryBuilder);
        this.converter = new ClassConverter<>(cacheUniqueId.getAClass(), cacheUniqueId, getCacheType());
        this.executor = executor;
    }

    @Override
    public LockKey getLockKey(long primaryKey) {
        return lockKey.createLockKey(String.valueOf(primaryKey));
    }

    @SuppressWarnings("unchecked")
    @Override
    public V cloneValue(V data) {
        return (V) data.clone(()-> convertClone(data));
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
        return converter.convert2Data(cacheValue);
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
    public ICacheDelaySource<K, V> createDelayUpdateSource() {
       throw new UnsupportedOperationException(getCacheType().name());
    }

    @Override
    public ICacheExecutor getExecutor() {
        return executor;
    }
}
