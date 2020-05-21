package com.game.cache.source;

import com.game.cache.data.IData;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.ClassInformation;
import com.game.cache.mapper.annotation.CacheClass;
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
    private final CacheClass cacheClass;
    private final LockKey lockKey;
    private final CacheKeyValueBuilder<PK, K> keyValueBuilder;
    private final ICacheSourceInteract<PK> sourceInteract;
    private final LockKey sharedLock;

    public CacheSource(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder, ICacheSourceInteract<PK> sourceInteract) {
        this.aClass = aClass;
        this.lockKey = LockKey.systemLockKey("cache").createLockKey(aClass.getSimpleName());
        this.keyValueBuilder = new CacheKeyValueBuilder<>(ClassInformation.get(aClass), primaryBuilder, secondaryBuilder);
        this.sourceInteract = sourceInteract;
        this.cacheClass = getKeyValueBuilder().getClassInformation().getCacheClass();
        this.sharedLock = LockKey.systemLockKey("cache").createLockKey("shared").createLockKey(cacheClass.cacheName());
    }

    @Override
    public LockKey getLockKey() {
        return lockKey;
    }

    @Override
    public Class<V> getAClass() {
        return aClass;
    }

    @Override
    public CacheClass getCacheClass() {
        return cacheClass;
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
        int primarySharedId = getPrimarySharedId();
        if (primarySharedId == 0){
            return getCollection0(key2Value, Collections.singletonList(primarySharedId));
        }
        String cacheName = getCacheName();
        CacheCollection cacheCollection = sourceInteract.removeCollection(primaryKey, cacheName, primarySharedId);
        if (cacheCollection != null){
            return cacheCollection;
        }
        boolean loginSharedLoad = sourceInteract.loginSharedLoad(primaryKey, cacheName);
        if (loginSharedLoad){
            List<Integer> primarySharedIds = sourceInteract.getPrimarySharedIds(cacheName, primarySharedId);
            return LockUtil.syncLock(sharedLock, "getCollection.V1", ()-> {
                Map<Integer, CacheCollection> sharedId2Collections = getCollection0(key2Value, primarySharedIds).groupBySharedId(primarySharedId);
                CacheCollection collection = sharedId2Collections.remove(primarySharedId);
                sourceInteract.addCollections(primaryKey, cacheName, sharedId2Collections);
                return collection;
            });
        }
        else {
            return LockUtil.syncLock(sharedLock, "getCollection.V2", ()-> getCollection0(key2Value, Collections.singletonList(primarySharedId)));
        }
    }

    protected abstract CacheCollection getCollection0(Map<String, Object> key2Value, List<Integer> primarySharedIds);

    public ICacheKeyValueBuilder<PK, K> getKeyValueBuilder() {
        return keyValueBuilder;
    }

    protected final String getCacheName() {
        return cacheClass.cacheName();
    }

    protected final int getPrimarySharedId() {
        return cacheClass.primarySharedId();
    }
}
