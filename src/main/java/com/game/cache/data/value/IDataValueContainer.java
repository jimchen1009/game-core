package com.game.cache.data.value;

import com.game.cache.data.IData;
import com.game.cache.data.IDataContainer;
import com.game.common.util.Holder;

public interface IDataValueContainer<K, V extends IData<K>> extends IDataContainer<K, K, V> {

    V get(K primaryKey);

    Holder<V> getNoCache(K primaryKey);

    V replace(V value);

    V remove(K primaryKey);
}
