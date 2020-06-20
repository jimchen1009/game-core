package com.game.cache.dao;

import com.game.cache.data.DataSourceBuilder;
import com.game.cache.data.IData;
import com.game.cache.data.IDataLifePredicate;
import com.game.cache.data.IDataSource;
import com.game.cache.data.map.DataMapContainer;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.source.interact.ICacheDBLifeInteract;

public class DataMapDaoBuilder<K, V extends IData<K>>  extends DataDaoBuilder{

	public DataMapDaoBuilder(Class<V> aClass, IKeyValueBuilder<K> secondaryBuilder) {
		super(aClass, secondaryBuilder);
	}

	public DataMapDaoBuilder<K, V> setLoadPredicate(IDataLifePredicate loadPredicate) {
		this.lifePredicate = loadPredicate;
		return this;
	}

	public DataMapDaoBuilder<K, V> setCacheLoginPredicate(ICacheDBLifeInteract cacheLifeInteract) {
		this.cacheLifeInteract = cacheLifeInteract;
		return this;
	}

	@SuppressWarnings("unchecked")
	public IDataMapDao<K, V> createNoCache(){
		DataSourceBuilder<K, V> dataSourceBuilder = newDataSourceBuilder();
		IDataSource<K, V> dataSource = dataSourceBuilder.createNoDelay();
		DataMapDao<K, V> dataMapDao = new DataMapDao<>(dataSource);
		return daoManager.addMapDao(cacheUniqueKey, dataMapDao);
	}

	@SuppressWarnings("unchecked")
	public IDataCacheMapDao<K, V> createCache(){
		DataSourceBuilder<K, V> dataSourceBuilder = newDataSourceBuilder();
		IDataSource<K, V> dataSource = dataSourceBuilder.createNoDelay();
		DataMapContainer<K, V> container = new DataMapContainer<>(dataSourceBuilder.create(), getLifePredicate());
		DataCacheMapDao<K, V> cacheMapDao = new DataCacheMapDao<>(dataSource, container);
		return (IDataCacheMapDao<K, V>)daoManager.addMapDao(cacheUniqueKey, cacheMapDao);
	}
}