package com.game.cache.data.map;

import com.game.cache.data.IData;
import com.game.cache.data.IDataContainer;

public interface IDataMap<K, V extends IData<K>>  extends IDataContainer<K, V >  {
}
