package com.game.cache.dao;

import com.game.cache.data.IData;

public interface IDataValueDao<PK, V extends IData<PK>> {

    V get(PK primaryKey);

    V getNotCache(PK primaryKey);

    V replace(V value);

    V delete(PK primaryKey);
}
