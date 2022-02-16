package com.game.core.cache.dao;

import com.game.core.cache.ClassConfig;
import com.game.core.cache.data.DataSourceUtil;
import com.game.core.cache.data.IData;
import com.game.core.cache.data.IDataSource;
import com.game.core.cache.data.value.DataValueContainer;
import com.game.core.cache.key.KeyValueBuilder;

public class DataValueDaoBuilder <V extends IData<Long>>  extends DataDaoBuilder{

	DataValueDaoBuilder(Class<V> aClass) {
		super(aClass, new KeyValueBuilder.ONE<>());
	}


	@SuppressWarnings("unchecked")
	public IDataCacheValueDao<V> getCacheInstance(){
		IDataSource<Long, V> dataSource = DataSourceUtil.createDataSource(createCacheSource());
		DataValueContainer<V> container = new DataValueContainer<>(dataSource, lifePredicate, daoManager.getExecutor());
		IDataCacheValueDao<V> cacheMapDao = new DataCacheValueDao<>(container);
		return daoManager.addCacheValueDao(cacheMapDao);
	}

	@SuppressWarnings("unchecked")
	public IDataValueDao<V> createDirectInstance(){
		ClassConfig classConfig = this.classConfig.cloneConfig().setDelayUpdate(false).setRedisSupport(false);
		IDataSource<Long, V> dataSource0 = DataSourceUtil.createDataSource(createCacheSource(classConfig));
		return new DataValueDao<>(dataSource0);
	}
}
