package com.game.core.cache.dao;

import com.game.common.util.Holder;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.data.DataSourceUtil;
import com.game.core.cache.data.IData;
import com.game.core.cache.data.value.IDataValueContainer;

import java.util.function.Consumer;

class DataCacheValueDao<V extends IData<Long>> implements IDataCacheValueDao<V> {

    private final IDataValueContainer<V> valueContainer;

    public DataCacheValueDao(IDataValueContainer<V> valueContainer) {
        this.valueContainer = valueContainer;
    }

    @Override
    public V get(long primaryKey) {
        return valueContainer.get(primaryKey);
    }

    @Override
    public ICacheUniqueId getCacheUniqueId() {
        return valueContainer.getDataSource().getCacheUniqueId();
    }

    @Override
    public boolean existCache(long primaryKey) {
        return valueContainer.existCache(primaryKey);
    }

    @Override
    public V getNotCache(long primaryKey) {
        Holder<V> holder = valueContainer.getNoCache(primaryKey, primaryKey);
        if (holder != null){
            return holder.getValue();
        }
        return DataSourceUtil.getNotCache(valueContainer.getDataSource(), primaryKey, primaryKey);
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
