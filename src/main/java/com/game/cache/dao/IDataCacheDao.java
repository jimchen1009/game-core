package com.game.cache.dao;

import java.util.function.Consumer;

public interface IDataCacheDao{

    boolean existCache(long primaryKey);

    boolean flushAll(long currentTime);

    void flushOne(long primaryKey, long currentTime, Consumer<Boolean> consumer);
}
