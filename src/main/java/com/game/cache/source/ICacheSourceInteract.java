package com.game.cache.source;

import java.util.List;
import java.util.Map;

public interface ICacheSourceInteract<PK> extends ICacheLoginPredicate<PK>{

    /**
     * 抢夺加载的数据内容
     * @param tableName
     * @param primaryKey
     * @param collections
     */
    void addCollections(PK primaryKey, String tableName, Map<Integer, CacheCollection> collections);

    /**
     * 获取被抢夺加载的内容
     * @param tableName
     * @param primaryKey
     * @return
     */
    CacheCollection removeCollection(PK primaryKey, String tableName, int primarySharedId);

    /**
     *
     * @param tableName
     * @param primarySharedId
     * @return
     */
    List<Integer> getPrimarySharedIds(String tableName, int primarySharedId);
}
