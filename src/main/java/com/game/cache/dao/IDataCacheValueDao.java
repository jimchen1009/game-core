package com.game.cache.dao;

import com.game.cache.data.IData;

public interface IDataCacheValueDao<V extends IData<Long>> extends IDataValueDao<V>, IDataCacheDao {

    V getNotCache(long id);
}
