package com.game.cache.dao;

import com.game.cache.data.IData;

public interface IDataValueDao<V extends IData<Long>> {

    V replace(V value);

    V delete(long primaryKey);

    V get(long primaryKey);

}
