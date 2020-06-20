package com.game.cache.dao;

import com.game.cache.data.IData;
import com.game.cache.data.IDataSource;
import com.game.cache.data.value.IDataValueContainer;
import com.game.cache.exception.CacheException;
import com.game.common.lock.LockUtil;
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
    public V get(long primaryKey) {
        return valueContainer.get(primaryKey);
    }

    @Override
    public boolean existCache(long primaryKey) {
        return valueContainer.existCache(primaryKey);
    }

    @Override
    public V getNotCache(long primaryKey) {
        Holder<V> holder = valueContainer.getNoCache(primaryKey);
        if (holder != null){
            return holder.getValue();
        }
        holder = LockUtil.syncLock(dataSource.getLockKey(primaryKey), "getAllNotCache", () -> new Holder<>(dataSource.get(primaryKey, primaryKey)));
        if (holder == null){
            throw new CacheException("primaryKey:%s getNotCache error", primaryKey);
        }
        return holder.getValue();
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
    public V delete(long primaryKey) {
        return valueContainer.remove(primaryKey);
    }
}
