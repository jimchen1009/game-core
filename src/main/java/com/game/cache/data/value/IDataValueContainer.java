package com.game.cache.data.value;

import com.game.cache.data.IData;
import com.game.cache.data.IDataContainer;
import com.game.common.util.Holder;

public interface IDataValueContainer<V extends IData<Long>> extends IDataContainer<Long, V> {

    V get(long primaryKey);

    Holder<V> getNoCache(long primaryKey);

    V replace(V value);

    V remove(long primaryKey);
}
