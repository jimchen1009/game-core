package com.game.core.cache.dao;

import com.game.core.cache.data.IData;

public interface IDataValueDao<V extends IData<Long>> {

    V replace(V value);

    V delete(long primaryKey);

    V get(long primaryKey);

}
