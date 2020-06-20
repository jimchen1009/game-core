package com.game.cache.data.value;

import com.game.cache.data.DataContainer;
import com.game.cache.data.IData;
import com.game.cache.data.IDataLifePredicate;
import com.game.cache.data.IDataSource;
import com.game.common.util.Holder;

public class DataValueContainer<V extends IData<Long>> extends DataContainer<Long, V> implements IDataValueContainer<V> {

    public DataValueContainer(IDataSource<Long, V> dataSource, IDataLifePredicate loadPredicate) {
        super(dataSource, loadPredicate);
    }

    @Override
    public V get(long primaryKey) {
        return get(primaryKey, primaryKey);
    }

    @Override
    public Holder<V> getNoCache(long primaryKey) {
        return getNoCache(primaryKey, primaryKey);
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
