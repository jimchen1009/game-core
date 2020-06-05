package com.game.cache.source;

import com.game.cache.data.IData;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.ClassConfig;
import com.game.cache.mapper.ClassInformation;
import com.game.cache.source.executor.ICacheSource;
import com.game.common.lock.LockKey;

public abstract class CacheSource<PK, K, V extends IData<K>> implements ICacheSource<PK, K, V> {

    private static final Object[] EMPTY = new Object[0];

    private final Class<V> aClass;
    private final LockKey lockKey;
    protected final CacheKeyValueBuilder<PK, K> keyValueBuilder;

    public CacheSource(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder) {
        this.aClass = aClass;
        this.lockKey = LockKey.systemLockKey("cache").createLockKey(aClass.getSimpleName());
        this.keyValueBuilder = new CacheKeyValueBuilder<>(ClassInformation.get(aClass), primaryBuilder, secondaryBuilder);
    }

    @Override
    public LockKey getLockKey(PK primaryKey) {
        Object[] objects = keyValueBuilder.toPrimaryKeyValue(primaryKey);
        return lockKey.createLockKey(objects[0].toString());
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
}
