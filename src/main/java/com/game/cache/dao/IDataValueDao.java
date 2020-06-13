package com.game.cache.dao;

import com.game.cache.data.IData;

public interface IDataValueDao<V extends IData<Long>> {

    V get(long primaryKey);

    V getNotCache(long primaryKey);

    V replace(V value);

    V delete(long primaryKey);
}
