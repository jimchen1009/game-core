package com.game.cache.data.map;

import com.game.cache.data.DataContainer;
import com.game.cache.data.IData;
import com.game.cache.data.IDataLoadPredicate;
import com.game.cache.data.IDataSource;

public class DataMapContainer<PK, K, V extends IData<K>> extends DataContainer<PK,K,V> implements IDataMap<PK, K, V>{

    public DataMapContainer(IDataSource<PK, K, V> dataSource, IDataLoadPredicate<PK> loadPredicate) {
        super(dataSource, loadPredicate);
    }
}
