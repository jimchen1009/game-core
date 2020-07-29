package com.game.core.cache.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

class DataSourceStatistic<K, V extends IData<K>> extends DataSourceDecorator<K, V>{

    private static final Logger logger = LoggerFactory.getLogger(DataSourceStatistic.class);

    public DataSourceStatistic(IDataSource<K, V> dataSource) {
        super(dataSource);
    }

    @Override
    protected void onGet(long primaryKey, K secondaryKey, V value) {
    }


    @Override
    protected void onGetAll(long primaryKey, List<V> values) {
    }

    @Override
    protected void onReplaceOne(long primaryKey, V value, boolean isSuccess) {
    }

    @Override
    protected void onReplaceBatch(long primaryKey, Collection<V> values, boolean isSuccess) {
    }

    @Override
    protected void onDeleteOne(long primaryKey, K secondaryKey, boolean isSuccess) {
    }

    @Override
    protected void onDeleteBatch(long primaryKey, Collection<K> secondaryKeys, boolean isSuccess) {
    }

    @Override
    protected void onGetCollection(long primaryKey, DataCollection<K, V> collection) {
    }

    @Override
    protected boolean decoratorEnable() {
        return logger.isTraceEnabled();
    }
}
