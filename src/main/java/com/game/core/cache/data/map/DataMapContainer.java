package com.game.core.cache.data.map;

import com.game.core.cache.data.DataContainer;
import com.game.core.cache.data.IData;
import com.game.core.cache.data.IDataLifePredicate;
import com.game.core.cache.data.IDataSource;
import com.game.core.cache.source.executor.ICacheExecutor;

public class DataMapContainer<K, V extends IData<K>> extends DataContainer<K,V> implements IDataMap<K, V>{

    public DataMapContainer(IDataSource<K, V> dataSource, IDataLifePredicate loadPredicate, ICacheExecutor executor) {
        super(dataSource, loadPredicate, executor);
    }
}
