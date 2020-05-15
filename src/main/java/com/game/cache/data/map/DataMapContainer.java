package com.game.cache.data.map;

import com.game.cache.data.Data;
import com.game.cache.data.DataContainer;
import com.game.cache.data.IDataSource;

public class DataMapContainer<PK, K, V extends Data<K>> extends DataContainer<PK,K,V> implements IDataMap<PK, K, V>{

    public DataMapContainer(IDataSource<PK, K, V> dataSource) {
        super(dataSource);
    }
}
