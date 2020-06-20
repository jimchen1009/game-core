package com.game.cache.dao;

import com.game.cache.data.DataSourceBuilder;
import com.game.cache.data.IData;
import com.game.cache.data.IDataLifePredicate;
import com.game.cache.data.IDataSource;
import com.game.cache.data.value.DataValueContainer;
import com.game.cache.key.KeyValueHelper;
import com.game.cache.source.interact.ICacheDBLifeInteract;

public class DataValueDaoBuilder <V extends IData<Long>>  extends DataDaoBuilder{

	public DataValueDaoBuilder(Class<V> aClass) {
		super(aClass, KeyValueHelper.LongBuilder);
	}

	public DataValueDaoBuilder<V> setLoadPredicate(IDataLifePredicate loadPredicate) {
		this.lifePredicate = loadPredicate;
		return this;
	}

	public DataValueDaoBuilder<V> setCacheLoginPredicate(ICacheDBLifeInteract loginSharedLoad) {
		this.cacheLifeInteract = loginSharedLoad;
		return this;
	}

	@SuppressWarnings("unchecked")
	public IDataValueDao<V> buildNoCache(){
		DataSourceBuilder<Long, V> dataSourceBuilder = newDataSourceBuilder();
		IDataSource<Long, V> dataSource = dataSourceBuilder.createNoDelay();
		DataValueDao<V> dataValueDao = new DataValueDao<>(dataSource);
		return daoManager.addValueDao(cacheUniqueKey, dataValueDao);
	}

	@SuppressWarnings("unchecked")
	public IDataCacheValueDao<V> buildCache(DataValueDaoBuilder<V> builder){
		DataSourceBuilder<Long, V> dataSourceBuilder = newDataSourceBuilder();
		IDataSource<Long, V> dataSource = dataSourceBuilder.createNoDelay();
		DataValueContainer<V> container = new DataValueContainer<>(dataSourceBuilder.create(), getLifePredicate());
		DataCacheValueDao<V> cacheValueDao = new DataCacheValueDao<>(dataSource, container);
		return (IDataCacheValueDao<V>)daoManager.addValueDao(cacheUniqueKey, cacheValueDao);
	}
}
