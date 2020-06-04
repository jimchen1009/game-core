package com.game.cache.source;

import com.game.cache.data.IData;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.ClassConfig;
import com.game.cache.mapper.ClassInformation;
import com.game.cache.source.executor.ICacheSource;
import com.game.common.lock.LockKey;
import com.game.common.lock.LockUtil;

import java.util.Collection;
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
    public Class<V> getAClass() {
        return aClass;
    }

    @Override
    public ClassConfig getClassConfig() {
        return keyValueBuilder.getClassInformation().getClassConfig();
    }

    @Override
    public Map<String, Object> get(PK primaryKey, K secondaryKey) {
        return get0(primaryKey, secondaryKey);
    }

    protected abstract Map<String, Object> get0(PK primaryKey, K secondaryKey);

    @Override
    public Collection<Map<String, Object>> getAll(PK primaryKey) {
        return LockUtil.syncLock(getSharedLockKey(primaryKey), "getAll", ()-> getAll0(primaryKey));
    }

    protected abstract Collection<Map<String, Object>> getAll0(PK primaryKey);

    @Override
    public final CacheCollection getCollection(PK primaryKey) {
        int primarySharedId = getClassConfig().primarySharedId;
        if (primarySharedId == 0){
            return LockUtil.syncLock(getSharedLockKey(primaryKey), "getCollection.V0", ()-> getPrimaryCollection(primaryKey));
        }
        String tableName = getClassConfig().tableName;
        CacheCollection cacheCollection = sourceInteract.removeCollection(primaryKey, tableName, primarySharedId);
        if (cacheCollection != null){
            return cacheCollection;
        }
        boolean loginSharedLoad = sourceInteract.loginSharedLoadTable(primaryKey, tableName);
        if (loginSharedLoad){
            List<Integer> primarySharedIds = sourceInteract.getPrimarySharedIds(tableName, primarySharedId);
            if (primarySharedIds.size() == 1){
                return LockUtil.syncLock(getSharedLockKey(primaryKey), "getCollection.V1", ()-> getPrimaryCollection(primaryKey));
            }
            else {
                return LockUtil.syncLock(getSharedLockKey(primaryKey), "getCollection.V2", ()-> {
                    Map<Integer, CacheCollection> sharedId2Collections = getSharedCollections(primaryKey, primarySharedIds);
                    CacheCollection collection = sharedId2Collections.remove(primarySharedId);
                    sourceInteract.addCollections(primaryKey, tableName, sharedId2Collections);
                    return collection;
                });
            }
        }
        else {
            return LockUtil.syncLock(getSharedLockKey(primaryKey), "getCollection.V3", ()-> getPrimaryCollection(primaryKey));
        }
    }

    protected abstract CacheCollection getPrimaryCollection(PK primaryKey);

    protected abstract Map<Integer, CacheCollection> getSharedCollections(PK primaryKey, List<Integer> primarySharedIds);

    public ICacheKeyValueBuilder<PK, K> getKeyValueBuilder() {
        return keyValueBuilder;
    }
}
