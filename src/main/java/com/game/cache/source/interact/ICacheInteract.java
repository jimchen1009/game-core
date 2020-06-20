package com.game.cache.source.interact;

import com.game.cache.ICacheUniqueKey;

import java.util.Map;

public interface ICacheInteract<T> extends ICacheLifeInteract {

    /**
     * 抢夺加载的数据内容
     * @param cacheDaoUnique
     * @param primaryKey
     * @param collections
     */
    void addCollections(long primaryKey, ICacheUniqueKey cacheDaoUnique, Map<Integer, T> collections);

    /**
     * 获取被抢夺加载的内容
     * @param cacheDaoUnique
     * @param primaryKey
     * @return
     */
    T removeCollection(long primaryKey, ICacheUniqueKey cacheDaoUnique);
}
