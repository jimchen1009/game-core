package com.game.cache.data.value;

import com.game.cache.data.DataContainer;
import com.game.cache.data.IData;
import com.game.cache.data.IDataLoadPredicate;
import com.game.cache.data.IDataSource;
import com.game.common.util.Holder;

public class DataValueContainer<PK, V extends IData<PK>> extends DataContainer<PK,PK,V> implements IDataValueContainer<PK, V> {

    public DataValueContainer(IDataSource<PK, PK, V> dataSource, IDataLoadPredicate<PK> loadPredicate) {
        super(dataSource, loadPredicate);
    }

    @Override
    public V get(PK primaryPKey) {
        return get(primaryPKey, primaryPKey);
    }

    @Override
    public Holder<V> getNoCache(PK primaryPKey) {
        return getNoCache(primaryPKey, primaryPKey);
    }

    @Override
    public V replace(V value) {
        return replaceOne(value.secondaryKey(), value);
    }

    @Override
    public V remove(PK primaryPKey) {
        return removeOne(primaryPKey, primaryPKey);
    }
}
