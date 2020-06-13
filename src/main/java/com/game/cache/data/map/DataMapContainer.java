package com.game.cache.data.map;

import com.game.cache.data.DataContainer;
import com.game.cache.data.IData;
import com.game.cache.data.IDataLoadPredicate;
import com.game.cache.data.IDataSource;

public class DataMapContainer<K, V extends IData<K>> extends DataContainer<K,V> implements IDataMap<K, V>{

    public DataMapContainer(IDataSource<K, V> dataSource, IDataLoadPredicate loadPredicate) {
        super(dataSource, loadPredicate);
    }
}
