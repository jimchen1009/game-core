package com.game.cache.source;

import com.game.cache.CacheUniqueKey;
import com.game.cache.ICacheUniqueKey;
import com.game.cache.data.IData;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.ClassConverter;
import com.game.cache.mapper.IClassConverter;
import com.game.cache.source.executor.ICacheSource;
import com.game.common.lock.LockKey;

import java.util.Map;
import java.util.function.Consumer;

public abstract class CacheSource<K, V extends IData<K>> implements ICacheSource<K, V> {

    private static final Object[] EMPTY = new Object[0];

    private final CacheUniqueKey cacheUniqueKey;
    private final LockKey lockKey;
    protected final CacheKeyValueBuilder<K> keyValueBuilder;
    protected final IClassConverter<K, V> converter;


    public CacheSource(CacheUniqueKey cacheUniqueKey, IKeyValueBuilder<K> secondaryBuilder) {
        this.cacheUniqueKey = cacheUniqueKey;
        Class<V> aClass = cacheUniqueKey.getAClass();
        this.lockKey = LockKey.systemLockKey("cache").createLockKey(aClass.getSimpleName());
        this.keyValueBuilder = new CacheKeyValueBuilder<>(cacheUniqueKey, secondaryBuilder);
        this.converter = new ClassConverter<>(aClass, getCacheType());
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
        return cacheUniqueKey.getAClass();
    }

    @Override
    public ICacheUniqueKey getCacheUniqueKey() {
        return cacheUniqueKey;
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
}
