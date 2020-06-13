package com.game.cache.dao;

import com.game.cache.ICacheDaoUnique;
import com.game.cache.source.CacheCollection;
import com.game.cache.source.ICacheLoginPredicate;
import com.game.cache.source.ICacheSourceInteract;
import com.game.common.util.ConcurrentWeakReferenceMap;

import java.util.List;
import java.util.Map;

public class CacheInteraction implements ICacheSourceInteract {

    private final DataDaoManager daoManager;
    private final ICacheLoginPredicate loginSharedLoad;
    private final ConcurrentWeakReferenceMap<String, Map<Integer, CacheCollection>> weakReferenceMap;

    public CacheInteraction(DataDaoManager daoManager, ICacheLoginPredicate loginSharedLoad) {
        this.daoManager = daoManager;
        this.loginSharedLoad = loginSharedLoad;
        this.weakReferenceMap = new ConcurrentWeakReferenceMap<>();
    }

    @Override
    public boolean loginSharedLoadTable(long primaryKey, ICacheDaoUnique cacheDaoUnique) {
        return loginSharedLoad.loginSharedLoadTable(primaryKey, cacheDaoUnique);
    }

    @Override
    public boolean loginSharedLoadRedis(long primaryKey, int redisSharedId) {
        return loginSharedLoad.loginSharedLoadRedis(primaryKey, redisSharedId);
    }

    @Override
    public void addCollections(long primaryKey, ICacheDaoUnique cacheDaoUnique, Map<Integer, CacheCollection> collections) {
        if (collections == null || collections.isEmpty()){
            return;
        }
        String format = String.format("%s_%s", cacheDaoUnique.getTableName(), primaryKey);
        weakReferenceMap.put(format, collections);
        List<ICacheDaoUnique> cacheDaoUniqueList = cacheDaoUnique.sharedCacheDaoUniqueList();
        for (ICacheDaoUnique daoUnique : cacheDaoUniqueList) {
            IDataCacheMapDao cacheMapDao = daoManager.getDataCacheMapDao(daoUnique);
            if (cacheMapDao == null){
                IDataCacheValueDao cacheValueDao = daoManager.getDataCacheValueDao(daoUnique);
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
    public CacheCollection removeCollection(long primaryKey, ICacheDaoUnique cacheDaoUnique, int primarySharedId) {
        String format = String.format("%s_%s", cacheDaoUnique.getTableName(), primaryKey);
        Map<Integer, CacheCollection> collections = weakReferenceMap.get(format);
        return collections == null ? null : collections.remove(primarySharedId);
    }
}
