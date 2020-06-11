package com.game.cache.dao;

import com.game.cache.mapper.ClassConfig;
import com.game.cache.source.CacheCollection;
import com.game.cache.source.ICacheLoginPredicate;
import com.game.cache.source.ICacheSourceInteract;
import com.game.common.util.ConcurrentWeakReferenceMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CacheInteraction<PK> implements ICacheSourceInteract<PK> {

    private final DataDaoManager daoManager;
    private final ICacheLoginPredicate<PK> loginSharedLoad;
    private final ConcurrentWeakReferenceMap<PK, Map<Integer, CacheCollection>> weakReferenceMap;

    public CacheInteraction(DataDaoManager daoManager, ICacheLoginPredicate<PK> loginSharedLoad) {
        this.daoManager = daoManager;
        this.loginSharedLoad = loginSharedLoad;
        this.weakReferenceMap = new ConcurrentWeakReferenceMap<>();
    }

    @Override
    public boolean loginSharedLoadTable(PK primaryKey, String tableName) {
        return loginSharedLoad.loginSharedLoadTable(primaryKey, tableName);
    }

    @Override
    public boolean loginSharedLoadRedis(PK primaryKey, int redisSharedId) {
        return loginSharedLoad.loginSharedLoadRedis(primaryKey, redisSharedId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addCollections(PK primaryKey, String tableName, Map<Integer, CacheCollection> collections) {
        if (collections == null || collections.isEmpty()){
            return;
        }
        weakReferenceMap.put(primaryKey, collections);
        List<Integer> primarySharedIds = new ArrayList<>(collections.keySet());
        for (Integer primarySharedId : primarySharedIds) {
            ClassConfig classConfig = ClassConfig.getConfig(tableName, primarySharedId);
            IDataCacheMapDao cacheMapDao = daoManager.getDataCacheMapDao(classConfig.className);
            if (cacheMapDao == null){
                IDataCacheValueDao cacheValueDao = daoManager.getDataCacheValueDao(classConfig.className);
                if (cacheValueDao != null){
                    cacheValueDao.get(primaryKey);
                }
            }
            else {
                cacheMapDao.getAll(primaryKey);
            }
        }
    }

    @Override
    public CacheCollection removeCollection(PK primaryKey, String tableName, int primarySharedId) {
        Map<Integer, CacheCollection> collections = weakReferenceMap.get(primaryKey);
        return collections == null ? null : collections.remove(primarySharedId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Integer> getPrimarySharedIds(PK primaryKey, String tableName, int primarySharedId) {
        List<ClassConfig> sharedConfigList = ClassConfig.getPrimarySharedConfigList(tableName);
        List<Integer> primarySharedIds = new ArrayList<>();
        for (ClassConfig classConfig : sharedConfigList) {
            if (classConfig.primarySharedId == primarySharedId){
                primarySharedIds.add(primarySharedId);
                continue;
            }
            IDataCacheMapDao cacheMapDao = daoManager.getDataCacheMapDao(classConfig.className);
            if (cacheMapDao == null){
                IDataCacheValueDao cacheValueDao = daoManager.getDataCacheValueDao(classConfig.className);
                if (cacheValueDao != null && !cacheValueDao.existCache(primaryKey)){
                    primarySharedIds.add(classConfig.primarySharedId);
                }
            }
            else if (!cacheMapDao.existCache(primaryKey)){
                primarySharedIds.add(classConfig.primarySharedId);
            }
        }
        return primarySharedIds;
    }
}
