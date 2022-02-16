package com.game.core.cache.data.value;

import com.game.core.cache.data.IData;
import com.game.core.cache.data.IDataContainer;

public interface IDataValueContainer<V extends IData<Long>> extends IDataContainer<Long, V> {

    V get(long primaryKey);

    V replace(V value);

    V remove(long primaryKey);
}
