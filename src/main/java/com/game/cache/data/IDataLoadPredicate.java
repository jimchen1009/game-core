package com.game.cache.data;

public interface IDataLoadPredicate {

    /**
     * 加载缓存数据回调
     * @param primaryKey
     */
    void onPredicateCacheLoaded(long primaryKey);

    /**
     * 是否没有缓存数据
     * @param primaryKey
     * @return
     */
    boolean predicateNoCache(long primaryKey);
}
