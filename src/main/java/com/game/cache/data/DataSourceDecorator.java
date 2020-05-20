package com.game.cache.data;

import com.game.cache.mapper.IClassConverter;
import com.game.common.lock.LockKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

abstract class DataSourceDecorator<PK, K, V extends IData<K>> implements IDataSource<PK, K, V>{

    private static final Logger logger = LoggerFactory.getLogger(DataSourceDecorator.class);

    protected final IDataSource<PK, K, V> dataSource;

    public DataSourceDecorator(IDataSource<PK, K, V> dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public LockKey getLockKey() {
        return dataSource.getLockKey();
    }

    @Override
    public V get(PK primaryKey, K secondaryKey) {
        V value = dataSource.get(primaryKey, secondaryKey);
        if (decoratorEnable()) {
            onGet(primaryKey, secondaryKey, value);
        }
        return value;
    }

    protected abstract void onGet(PK primaryKey, K secondaryKey, V value);

    @Override
    public List<V> getAll(PK primaryKey) {
        List<V> values = dataSource.getAll(primaryKey);
        if (decoratorEnable()) {
            onGetAll(primaryKey, values);
        }
        return values;
    }

    protected abstract void onGetAll(PK primaryKey, List<V> values);

    @Override
    public boolean replaceOne(PK primaryKey, V value) {
        boolean isSuccess = dataSource.replaceOne(primaryKey, value);
        if (decoratorEnable()) {
            onReplaceOne(primaryKey, value, isSuccess);
        }
        return isSuccess;
    }

    protected abstract void onReplaceOne(PK primaryKey, V value, boolean isSuccess);

    @Override
    public boolean replaceBatch(PK primaryKey, Collection<V> values) {
        boolean isSuccess = dataSource.replaceBatch(primaryKey, values);
        if (decoratorEnable()){
            onReplaceBatch(primaryKey, values, isSuccess);
        }
        return isSuccess;
    }

    protected abstract void onReplaceBatch(PK primaryKey, Collection<V> values, boolean isSuccess);

    @Override
    public boolean deleteOne(PK primaryKey, K secondaryKey) {
        boolean isSuccess = dataSource.deleteOne(primaryKey, secondaryKey);
        if (decoratorEnable()){
            onDeleteOne(primaryKey, secondaryKey, isSuccess);
        }
        return isSuccess;
    }

    protected abstract void onDeleteOne(PK primaryKey, K secondaryKey, boolean isSuccess);

    @Override
    public boolean deleteBatch(PK primaryKey, Collection<K> secondaryKeys) {
        boolean isSuccess = dataSource.deleteBatch(primaryKey, secondaryKeys);
        if (decoratorEnable()){
            onDeleteBatch(primaryKey, secondaryKeys, isSuccess);
        }
        return isSuccess;
    }

    protected abstract void onDeleteBatch(PK primaryKey, Collection<K> secondaryKeys, boolean isSuccess);

    @Override
    public DataCollection<K, V> getCollection(PK primaryKey) {
        DataCollection<K, V> collection = dataSource.getCollection(primaryKey);
        if (decoratorEnable()){
            onGetCollection(primaryKey, collection);
        }
        return collection;
    }

    protected abstract void onGetCollection(PK primaryKey, DataCollection<K, V> collection);


    @Override
    @SuppressWarnings("unchecked")
    public V cloneValue(V value) {
        return dataSource.cloneValue(value);
    }

    @Override
    public IClassConverter<K, V> getConverter() {
        return dataSource.getConverter();
    }

    protected abstract boolean decoratorEnable();
}
