package com.game.core.cache.data;

import com.game.common.lock.LockKey;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.mapper.IClassConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

abstract class DataSourceDecorator<K, V extends IData<K>> implements IDataSource<K, V>{

    private static final Logger logger = LoggerFactory.getLogger(DataSourceDecorator.class);

    protected final IDataSource<K, V> dataSource;

    public DataSourceDecorator(IDataSource<K, V> dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public ICacheUniqueId getCacheUniqueId() {
        return dataSource.getCacheUniqueId();
    }

    @Override
    public LockKey getLockKey(long primaryKey) {
        return dataSource.getLockKey(primaryKey);
    }

    @Override
    public V get(long primaryKey, K secondaryKey) {
        V value = dataSource.get(primaryKey, secondaryKey);
        if (decoratorEnable()) {
            onGet(primaryKey, secondaryKey, value);
        }
        return value;
    }

    protected abstract void onGet(long primaryKey, K secondaryKey, V value);

    @Override
    public List<V> getAll(long primaryKey) {
        List<V> values = dataSource.getAll(primaryKey);
        if (decoratorEnable()) {
            onGetAll(primaryKey, values);
        }
        return values;
    }

    protected abstract void onGetAll(long primaryKey, List<V> values);

    @Override
    public boolean replaceOne(long primaryKey, V data) {
        boolean isSuccess = dataSource.replaceOne(primaryKey, data);
        if (decoratorEnable()) {
            onReplaceOne(primaryKey, data, isSuccess);
        }
        return isSuccess;
    }

    protected abstract void onReplaceOne(long primaryKey, V value, boolean isSuccess);

    @Override
    public boolean replaceBatch(long primaryKey, Collection<V> dataList) {
        boolean isSuccess = dataSource.replaceBatch(primaryKey, dataList);
        if (decoratorEnable()){
            onReplaceBatch(primaryKey, dataList, isSuccess);
        }
        return isSuccess;
    }

    protected abstract void onReplaceBatch(long primaryKey, Collection<V> values, boolean isSuccess);

    @Override
    public boolean deleteOne(long primaryKey, K secondaryKey) {
        boolean isSuccess = dataSource.deleteOne(primaryKey, secondaryKey);
        if (decoratorEnable()){
            onDeleteOne(primaryKey, secondaryKey, isSuccess);
        }
        return isSuccess;
    }

    protected abstract void onDeleteOne(long primaryKey, K secondaryKey, boolean isSuccess);

    @Override
    public boolean deleteBatch(long primaryKey, Collection<K> secondaryKeys) {
        boolean isSuccess = dataSource.deleteBatch(primaryKey, secondaryKeys);
        if (decoratorEnable()){
            onDeleteBatch(primaryKey, secondaryKeys, isSuccess);
        }
        return isSuccess;
    }

    protected abstract void onDeleteBatch(long primaryKey, Collection<K> secondaryKeys, boolean isSuccess);

    @Override
    public DataCollection<K, V> getCollection(long primaryKey) {
        DataCollection<K, V> collection = dataSource.getCollection(primaryKey);
        if (decoratorEnable()){
            onGetCollection(primaryKey, collection);
        }
        return collection;
    }

    protected abstract void onGetCollection(long primaryKey, DataCollection<K, V> collection);


    @Override
    @SuppressWarnings("unchecked")
    public V cloneValue(V data) {
        return dataSource.cloneValue(data);
    }

    @Override
    public IClassConverter<K, V> getConverter() {
        return dataSource.getConverter();
    }

    protected abstract boolean decoratorEnable();

    @Override
    public boolean flushAll(long currentTime) {
        return dataSource.flushAll(currentTime);
    }

    @Override
    public void flushOne(long primaryKey, long currentTime, Consumer<Boolean> consumer) {
        dataSource.flushOne(primaryKey, currentTime, consumer);
    }
}
