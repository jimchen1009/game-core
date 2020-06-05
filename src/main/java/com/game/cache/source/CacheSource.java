package com.game.cache.source;

import com.game.cache.data.IData;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.ClassConfig;
import com.game.cache.mapper.ClassConverter;
import com.game.cache.mapper.ClassInformation;
import com.game.cache.mapper.IClassConverter;
import com.game.cache.source.executor.ICacheSource;
import com.game.common.lock.LockKey;

import java.util.Map;

public abstract class CacheSource<PK, K, V extends IData<K>> implements ICacheSource<PK, K, V> {

    private static final Object[] EMPTY = new Object[0];

    private final Class<V> aClass;
    private final LockKey lockKey;
    protected final CacheKeyValueBuilder<PK, K> keyValueBuilder;
    protected final IClassConverter<K, V> converter;


    public CacheSource(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder) {
        this.aClass = aClass;
        this.lockKey = LockKey.systemLockKey("cache").createLockKey(aClass.getSimpleName());
        this.keyValueBuilder = new CacheKeyValueBuilder<>(ClassInformation.get(aClass), primaryBuilder, secondaryBuilder);
        this.converter = new ClassConverter<>(aClass, getCacheType().getValueConvertMapper());
    }

    @Override
    public LockKey getLockKey(PK primaryKey) {
        Object[] objects = keyValueBuilder.toPrimaryKeyValue(primaryKey);
        return lockKey.createLockKey(objects[0].toString());
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
        return aClass;
    }

    @Override
    public ClassConfig getClassConfig() {
        return keyValueBuilder.getClassInformation().getClassConfig();
    }

    @Override
    public ICacheKeyValueBuilder<PK, K> getKeyValueBuilder() {
        return keyValueBuilder;
    }

    private V convertClone(V value){
        Map<String, Object> cacheValue = converter.convert2Cache(value);
        return converter.convert2Value(cacheValue);
    }
}
