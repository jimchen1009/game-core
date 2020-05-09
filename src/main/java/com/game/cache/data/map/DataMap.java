package com.game.cache.data.map;

import com.game.cache.data.Data;
import com.game.cache.data.DataContainer;
import com.game.cache.data.IDataSource;

public class DataMap<PK, K, V extends Data<K>> extends DataContainer<PK,K,V> implements IDataMap<PK, K, V>{

    public DataMap(PK primaryKey, IDataSource<PK, K, V> dataSource) {
        super(primaryKey, dataSource);
    }
}
