package com.game.core.cache.dao;

import com.game.core.cache.ClassConfig;
import com.game.core.cache.data.DataSourceUtil;
import com.game.core.cache.data.IData;
import com.game.core.cache.data.IDataSource;
import com.game.core.cache.data.map.DataMapContainer;
import com.game.core.cache.key.IKeyValueBuilder;
import com.game.core.cache.source.executor.ICacheSource;

public class DataMapDaoBuilder<K, V extends IData<K>>  extends DataDaoBuilder{

	public DataMapDaoBuilder(Class<V> aClass, IKeyValueBuilder<K> secondaryBuilder) {
		super(aClass, secondaryBuilder);
	}


	@SuppressWarnings("unchecked")
	public IDataCacheMapDao<K, V> createIfAbsent(){
		IDataSource<K, V> dataSource = DataSourceUtil.createDataSource(createCacheSource());
		DataMapContainer<K, V> container = new DataMapContainer<>(dataSource, lifePredicate, daoManager.getExecutor());
		ClassConfig classConfig = this.classConfig.cloneConfig().setDelayUpdate(false);
		IDataSource<K, V> dataSource0 = DataSourceUtil.createDataSource(createCacheSource(classConfig));
		DataCacheMapDao<K, V> cacheMapDao = new DataCacheMapDao<>(dataSource0, container);
		return daoManager.addCacheMapDao(cacheMapDao);
	}

	@SuppressWarnings("unchecked")
	public IDataMapDao<K, V> createDirectDao(){
		ClassConfig classConfig = this.classConfig.cloneConfig().setDelayUpdate(false).setRedisSupport(false);
		ICacheSource<K, V> cacheSource = createCacheSource(classConfig);
		IDataSource<K, V> dataSource = DataSourceUtil.createDataSource(cacheSource);
		return new DataMapDao<>(dataSource);
	}
}