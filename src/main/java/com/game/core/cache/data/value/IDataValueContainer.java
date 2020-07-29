package com.game.core.cache.data.value;

import com.game.core.cache.data.IData;
import com.game.core.cache.data.IDataContainer;
import com.game.common.util.Holder;

public interface IDataValueContainer<V extends IData<Long>> extends IDataContainer<Long, V> {

    V get(long primaryKey);

    Holder<V> getNoCache(long primaryKey);

    V replace(V value);

    V remove(long primaryKey);
}
