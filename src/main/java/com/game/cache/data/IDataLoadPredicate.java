package com.game.cache.data;

public interface IDataLoadPredicate<PK> {

    /**
     * 第一次加载数据标记回调
     * @param primaryKey
     */
    void onPredicateLoaded(PK primaryKey);

    /**
     * 是否第一次加载数据
     * @param primaryKey
     * @return
     */
    boolean predicateFirstTime(PK primaryKey);
}
