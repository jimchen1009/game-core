package com.game.cache.data.map;

import com.game.cache.data.IData;
import com.game.cache.data.IDataContainer;

public interface IDataMap<PK, K, V extends IData<K>>  extends IDataContainer<PK, K, V >  {
}
