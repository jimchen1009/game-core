package com.game.cache.dao;

import com.game.cache.data.IData;

public interface IDataCacheValueDao<PK, V extends IData<PK>> extends IDataValueDao<PK, V> {

    boolean existCache(PK primaryKey);

    V getNotCache(PK primaryKey);

    boolean flushAll();
}
