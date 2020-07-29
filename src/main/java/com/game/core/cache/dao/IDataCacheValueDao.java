package com.game.core.cache.dao;

import com.game.core.cache.data.IData;

public interface IDataCacheValueDao<V extends IData<Long>> extends IDataValueDao<V>, IDataCacheDao {

    V getNotCache(long id);
}
