package com.game.cache.data.value;

import com.game.cache.data.DataContainer;
import com.game.cache.data.IData;
import com.game.cache.data.IDataSource;
import com.game.common.util.Holder;

public class DataValueContainer<K, V extends IData<K>> extends DataContainer<K,K,V> implements IDataValueContainer<K, V> {

    public DataValueContainer(IDataSource<K, K, V> dataSource) {
        super(dataSource);
    }

    @Override
    public V get(K primaryKey) {
        return get(primaryKey, primaryKey);
    }

    @Override
    public Holder<V> getNoCache(K primaryKey) {
        return getNoCache(primaryKey, primaryKey);
    }

    @Override
    public V replace(V value) {
        return replaceOne(value.secondaryKey(), value);
    }

    @Override
    public V remove(K primaryKey) {
        return removeOne(primaryKey, primaryKey);
    }
}
