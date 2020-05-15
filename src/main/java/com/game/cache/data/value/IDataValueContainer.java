package com.game.cache.data.value;

import com.game.cache.data.IData;
import com.game.cache.data.IDataContainer;

public interface IDataValueContainer<K, V extends IData<K>> extends IDataContainer<K, K, V> {

    V get(K primaryKey);

    V set(K primaryKey, V value);
}
