package com.game.cache.data.map;

import com.game.cache.data.DataContainer;
import com.game.cache.data.IData;
import com.game.cache.data.IDataLifePredicate;
import com.game.cache.data.IDataSource;
import com.game.cache.source.executor.ICacheExecutor;

public class DataMapContainer<K, V extends IData<K>> extends DataContainer<K,V> implements IDataMap<K, V>{

    public DataMapContainer(IDataSource<K, V> dataSource, IDataLifePredicate loadPredicate, ICacheExecutor executor) {
        super(dataSource, loadPredicate, executor);
    }
}
