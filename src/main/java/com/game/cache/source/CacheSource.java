package com.game.cache.source;

import com.game.cache.data.IData;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.ClassConfig;
import com.game.cache.mapper.ClassInformation;
import com.game.cache.source.executor.ICacheExecutor;
import com.game.cache.source.executor.ICacheSource;
import com.game.common.lock.LockKey;
import com.game.common.lock.LockUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class CacheSource<PK, K, V extends IData<K>> implements ICacheSource<PK, K, V> {

    private static final Object[] EMPTY = new Object[0];

    private final Class<V> aClass;
    private final LockKey lockKey;
    private final CacheKeyValueBuilder<PK, K> keyValueBuilder;
    private final ICacheSourceInteract<PK> sourceInteract;

    public CacheSource(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder, ICacheSourceInteract<PK> sourceInteract) {
        this.aClass = aClass;
        this.lockKey = LockKey.systemLockKey("cache").createLockKey(aClass.getSimpleName());
        this.keyValueBuilder = new CacheKeyValueBuilder<>(ClassInformation.get(aClass), primaryBuilder, secondaryBuilder);
        this.sourceInteract = sourceInteract;
    }

    @Override
    public LockKey getLockKey(PK primaryKey) {
        Object[] objects = keyValueBuilder.getPrimaryBuilder().createValue(primaryKey);
        return lockKey.createLockKey("class").createLockKey(aClass.getSimpleName()).createLockKey(objects[0].toString());
}

    @Override
    public LockKey getSharedLockKey(PK primaryKey) {
        Object[] objects = keyValueBuilder.getPrimaryBuilder().createValue(primaryKey);
        ClassConfig classConfig = keyValueBuilder.getClassInformation().getClassConfig();
        return lockKey.createLockKey("table").createLockKey(classConfig.tableName).createLockKey(objects[0].toString());
    }

    @Override
    public boolean replaceOne(PK primaryKey, KeyCacheValue<K> keyCacheValue) {
        return false;
    }

    @Override
    public boolean replaceBatch(PK primaryKey, List<KeyCacheValue<K>> keyCacheValues) {
        return false;
    }

    @Override
    public boolean deleteOne(PK primaryKey, K secondaryKey) {
        return false;
    }

    @Override
    public boolean deleteBatch(PK primaryKey, Collection<K> secondaryKeys) {
        return false;
    }

    @Override
    public ICacheDelayUpdateSource<PK, K, V> createDelayUpdateSource(ICacheExecutor executor) {
        return null;
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
    public Map<String, Object> get(PK primaryKey, K secondaryKey) {
        Map<String, Object> key2Value = getKeyValueBuilder().createPrimarySecondaryKeyValue(primaryKey, secondaryKey);
        return get0(key2Value);
    }

    protected abstract Map<String, Object> get0(Map<String, Object> key2Value);

    @Override
    public Collection<Map<String, Object>> getAll(PK primaryKey) {
        Map<String, Object> key2Value = getKeyValueBuilder().createPrimaryKeyValue(primaryKey);
        return getAll0(key2Value);
    }

    protected abstract Collection<Map<String, Object>> getAll0(Map<String, Object> key2Value);

    @Override
    public final CacheCollection getCollection(PK primaryKey) {
        Map<String, Object> key2Value = getKeyValueBuilder().createPrimaryKeyValue(primaryKey);
        int primarySharedId = getClassConfig().primarySharedId;
        if (primarySharedId == 0){
            return getCollection0(key2Value, Collections.singletonList(primarySharedId));
        }
        String tableName = getClassConfig().tableName;
        CacheCollection cacheCollection = sourceInteract.removeCollection(primaryKey, tableName, primarySharedId);
        if (cacheCollection != null){
            return cacheCollection;
        }
        boolean loginSharedLoad = sourceInteract.loginSharedLoad(primaryKey, tableName);
        if (loginSharedLoad){
            List<Integer> primarySharedIds = sourceInteract.getPrimarySharedIds(tableName, primarySharedId);
            return LockUtil.syncLock(getSharedLockKey(primaryKey), "getCollection.V1", ()-> {
                Map<Integer, CacheCollection> sharedId2Collections = getCollection0(key2Value, primarySharedIds).groupBySharedId(primarySharedId);
                CacheCollection collection = sharedId2Collections.remove(primarySharedId);
                sourceInteract.addCollections(primaryKey, tableName, sharedId2Collections);
                return collection;
            });
        }
        else {
            return LockUtil.syncLock(getSharedLockKey(primaryKey), "getCollection.V2", ()-> getCollection0(key2Value, Collections.singletonList(primarySharedId)));
        }
    }

    protected abstract CacheCollection getCollection0(Map<String, Object> key2Value, List<Integer> primarySharedIds);

    public ICacheKeyValueBuilder<PK, K> getKeyValueBuilder() {
        return keyValueBuilder;
    }
}
