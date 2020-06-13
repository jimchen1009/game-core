package com.game.cache.dao;

import com.game.cache.data.IData;
import com.game.cache.data.IDataSource;
import com.game.cache.data.value.IDataValueContainer;
import com.game.common.util.Holder;

import java.util.function.Consumer;

class DataCacheValueDao<V extends IData<Long>> implements IDataCacheValueDao<V> {

    private final IDataSource<Long, V> dataSource;
    private final IDataValueContainer<V> valueContainer;

    public DataCacheValueDao(IDataSource<Long, V> dataSource, IDataValueContainer<V> valueContainer) {
        this.dataSource = dataSource;
        this.valueContainer = valueContainer;
    }

    @Override
    public V get(long id) {
        return valueContainer.get(id);
    }

    @Override
    public boolean existCache(long id) {
        return valueContainer.existCache(id);
    }

    @Override
    public V getNotCache(long id) {
        Holder<V> holder = valueContainer.getNoCache(id);
        if (holder != null){
            return holder.getValue();
        }
        return dataSource.get(id, id);
    }

    @Override
    public boolean flushAll(long currentTime) {
        return valueContainer.flushAll(currentTime);
    }

	@Override
	public void flushOne(long primaryKey, long currentTime, Consumer<Boolean> consumer) {
		valueContainer.flushOne(primaryKey, currentTime, consumer);
	}

	@Override
    public V replace(V value) {
        return valueContainer.replace(value);
    }

    @Override
    public V delete(long id) {
        return valueContainer.remove(id);
    }
}
