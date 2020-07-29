package com.game.core.cache.source.interact;

import com.game.core.cache.ICacheUniqueId;

import java.util.List;
import java.util.Map;

public interface ICacheInteract<T> extends ICacheLifeInteract {


    void removePrimary(long primaryKey);

    /**
     * 获取除了自己之外，其他的~
     * @param primaryKey
     * @param cacheDaoUnique
     * @return
     */
    List<ICacheUniqueId> getSharedCacheUniqueIdList(long primaryKey, ICacheUniqueId cacheDaoUnique);

    /**
     * 抢夺加载的数据内容
     * @param cacheDaoUnique
     * @param primaryKey
     * @param collections
     */
    void addCollections(long primaryKey, ICacheUniqueId cacheDaoUnique, Map<ICacheUniqueId, T> collections);

    /**
     * 获取被抢夺加载的内容
     * @param cacheDaoUnique
     * @param primaryKey
     * @return
     */
    T removeCollection(long primaryKey, ICacheUniqueId cacheDaoUnique);
}
