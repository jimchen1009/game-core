package com.game.cache.data.value;

import com.game.cache.data.IData;

public interface IDataValue<K, V extends IData<K>>{

    K primaryKey();

    V get();

    V set(V value);
}
