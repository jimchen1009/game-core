package com.game.cache.source;

import com.game.cache.ICacheDaoUnique;

public interface ICacheLoginPredicate  {
    /**
     * 抢夺登录的第一次加载
     * @param primaryKey
     */
    boolean loginSharedLoadTable(long primaryKey, ICacheDaoUnique cacheDaoUnique);


    boolean loginSharedLoadRedis(long primaryKey, int redisSharedId);
}
