package com.game.cache.dao;

import com.game.cache.source.CacheCollection;
import com.game.cache.source.ICacheLoginPredicate;
import com.game.cache.source.ICacheSourceInteract;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CacheInteraction<PK> implements ICacheSourceInteract<PK> {

    private final DataDaoManager daoManager;
    private final ICacheLoginPredicate<PK> loginSharedLoad;
    private WeakReference<Map<Integer, CacheCollection>> weakReference;

    public CacheInteraction(DataDaoManager daoManager, ICacheLoginPredicate<PK> loginSharedLoad) {
        this.daoManager = daoManager;
        this.loginSharedLoad = loginSharedLoad;
    }

    @Override
    public boolean loginSharedLoadTable(PK primaryKey, String tableName) {
        return loginSharedLoad.loginSharedLoadTable(primaryKey, tableName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addCollections(PK primaryKey, String tableName, Map<Integer, CacheCollection> collections) {
        if (collections == null || collections.isEmpty()){
            return;
        }
        this.weakReference = new WeakReference<>(collections);
        ClassesInformation information = daoManager.getClassesInformation();
        List<Integer> primarySharedIds = new ArrayList<>(collections.keySet());
        for (Integer primarySharedId : primarySharedIds) {
            Class<?> aClass = information.getClass(tableName, primarySharedId);
            IDataCacheMapDao cacheMapDao = daoManager.getDataCacheMapDao(aClass);
            if (cacheMapDao == null){
                IDataCacheValueDao cacheValueDao = daoManager.getDataCacheValueDao(aClass);
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
        if (weakReference == null){
            return null;
        }
        Map<Integer, CacheCollection> collections = weakReference.get();
        return collections == null ? null : collections.remove(primarySharedId);
    }

    @Override
    public List<Integer> getPrimarySharedIds(String tableName, int primarySharedId) {
        ClassesInformation information = daoManager.getClassesInformation();
        return information.getPrimarySharedIds(tableName, primarySharedId);
    }
}
