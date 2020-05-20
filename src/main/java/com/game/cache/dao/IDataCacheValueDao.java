package com.game.cache.dao;

import com.game.cache.data.IData;

public interface IDataCacheValueDao<PK, V extends IData<PK>> extends IDataValueDao<PK, V> {

    V getNotCache(PK primaryKey);
}
