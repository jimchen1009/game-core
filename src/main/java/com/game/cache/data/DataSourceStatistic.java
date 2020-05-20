package com.game.cache.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

class DataSourceStatistic<PK, K, V extends IData<K>> extends DataSourceDecorator<PK, K, V>{

    private static final Logger logger = LoggerFactory.getLogger(DataSourceStatistic.class);

    public DataSourceStatistic(IDataSource<PK, K, V> dataSource) {
        super(dataSource);
    }

    @Override
    protected void onGet(PK primaryKey, K secondaryKey, V value) {
    }


    @Override
    protected void onGetAll(PK primaryKey, List<V> values) {
    }

    @Override
    protected void onReplaceOne(PK primaryKey, V value, boolean isSuccess) {
    }

    @Override
    protected void onReplaceBatch(PK primaryKey, Collection<V> values, boolean isSuccess) {
    }

    @Override
    protected void onDeleteOne(PK primaryKey, K secondaryKey, boolean isSuccess) {
    }

    @Override
    protected void onDeleteBatch(PK primaryKey, Collection<K> secondaryKeys, boolean isSuccess) {
    }

    @Override
    protected void onGetCollection(PK primaryKey, DataCollection<K, V> collection) {
    }

    @Override
    protected boolean decoratorEnable() {
        return logger.isTraceEnabled();
    }
}
