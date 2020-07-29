package com.game.core.cache.dao;

import com.game.core.cache.ICacheUniqueId;

import java.util.function.Consumer;

public interface IDataCacheDao{

    ICacheUniqueId getCacheUniqueId();

    boolean existCache(long primaryKey);

    boolean flushAll(long currentTime);

    void flushOne(long primaryKey, long currentTime, Consumer<Boolean> consumer);
}
