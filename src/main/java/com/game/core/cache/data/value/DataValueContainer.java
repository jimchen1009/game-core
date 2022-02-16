package com.game.core.cache.data.value;

import com.game.core.cache.data.DataContainer;
import com.game.core.cache.data.IData;
import com.game.core.cache.data.IDataLifePredicate;
import com.game.core.cache.data.IDataSource;
import com.game.core.cache.source.executor.ICacheExecutor;
import com.game.common.util.Holder;

public class DataValueContainer<V extends IData<Long>> extends DataContainer<Long, V> implements IDataValueContainer<V> {

    public DataValueContainer(IDataSource<Long, V> dataSource, IDataLifePredicate loadPredicate, ICacheExecutor executor) {
        super(dataSource, loadPredicate, executor);
    }

    @Override
    public V get(long primaryKey) {
        return get(primaryKey, primaryKey);
    }

    @Override
    public V replace(V value) {
        return replaceOne(value.secondaryKey(), value);
    }

    @Override
    public V remove(long primaryKey) {
        return removeOne(primaryKey, primaryKey);
    }
}
