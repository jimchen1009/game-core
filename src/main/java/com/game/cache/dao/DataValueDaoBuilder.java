package com.game.cache.dao;

import com.game.cache.ClassConfig;
import com.game.cache.data.DataSourceUtil;
import com.game.cache.data.IData;
import com.game.cache.data.IDataSource;
import com.game.cache.data.value.DataValueContainer;
import com.game.cache.key.KeyValueHelper;

public class DataValueDaoBuilder <V extends IData<Long>>  extends DataDaoBuilder{

	public DataValueDaoBuilder(Class<V> aClass) {
		super(aClass, KeyValueHelper.LongBuilder);
	}


	@SuppressWarnings("unchecked")
	public IDataCacheValueDao<V> createIfAbsent(DataValueDaoBuilder<V> builder){
		IDataSource<Long, V> dataSource = DataSourceUtil.createDataSource(createCacheSource());
		DataValueContainer<V> container = new DataValueContainer<>(dataSource, lifePredicate);

		ClassConfig classConfig = this.classConfig.cloneConfig().setDelayUpdate(false);
		IDataSource<Long, V> dataSource0 = DataSourceUtil.createDataSource(createCacheSource(classConfig));

		IDataCacheValueDao<V> cacheMapDao = new DataCacheValueDao<>(dataSource0, container);
		return daoManager.addCacheValueDao(cacheMapDao);
	}

	@SuppressWarnings("unchecked")
	public IDataValueDao<V> createDirectDao(){
		ClassConfig classConfig = this.classConfig.cloneConfig().setDelayUpdate(false).setRedisSupport(false);
		IDataSource<Long, V> dataSource0 = DataSourceUtil.createDataSource(createCacheSource(classConfig));
		return new DataValueDao<>(dataSource0);
	}
}
