package com.game.core.cache.data.map;

import com.game.core.cache.data.IData;
import com.game.core.cache.data.IDataContainer;

public interface IDataMap<K, V extends IData<K>>  extends IDataContainer<K, V > {
}
