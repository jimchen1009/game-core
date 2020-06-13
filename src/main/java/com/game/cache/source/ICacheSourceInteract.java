package com.game.cache.source;

import com.game.cache.ICacheDaoUnique;

import java.util.Map;

public interface ICacheSourceInteract extends ICacheLoginPredicate{

    /**
     * 抢夺加载的数据内容
     * @param cacheDaoUnique
     * @param primaryKey
     * @param collections
     */
    void addCollections(long primaryKey, ICacheDaoUnique cacheDaoUnique, Map<Integer, CacheCollection> collections);

    /**
     * 获取被抢夺加载的内容
     * @param cacheDaoUnique
     * @param primaryKey
     * @return
     */
    CacheCollection removeCollection(long primaryKey, ICacheDaoUnique cacheDaoUnique, int primarySharedId);
}
