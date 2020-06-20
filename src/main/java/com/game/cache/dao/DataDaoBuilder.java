package com.game.cache.dao;

import com.game.cache.CacheUniqueKey;
import com.game.cache.CacheType;
import com.game.cache.config.ClassConfig;
import com.game.cache.data.DataSourceBuilder;
import com.game.cache.data.IData;
import com.game.cache.data.IDataLifePredicate;
import com.game.cache.exception.CacheException;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.source.compose.CacheComposeSource;
import com.game.cache.source.executor.ICacheSource;
import com.game.cache.source.interact.ICacheDBInteract;
import com.game.cache.source.interact.ICacheDBLifeInteract;
import com.game.cache.source.interact.ICacheRedisInteract;
import com.game.cache.source.mongodb.CacheMongoDBSource;
import com.game.cache.source.redis.CacheRedisSource;
import com.game.common.config.Configs;
import com.game.common.config.IConfig;

import java.util.List;
import java.util.Map;

public class DataDaoBuilder <K, V extends IData<K>> {

	protected CacheUniqueKey cacheUniqueKey;
	private final IKeyValueBuilder<K> secondaryBuilder;
	protected IDataLifePredicate lifePredicate;
	protected ICacheDBLifeInteract cacheLifeInteract;

	protected DataDaoManager daoManager;

	protected DataDaoBuilder(Class<V> aClass, IKeyValueBuilder<K> secondaryBuilder) {
		this.cacheUniqueKey = CacheUniqueKey.create(aClass);
		this.secondaryBuilder = secondaryBuilder;
	}

	void setDaoManager(DataDaoManager daoManager) {
		this.daoManager = daoManager;
	}

	public void setAppendKeyList(List<Map.Entry<String, Object>> appendKeyList) {
		cacheUniqueKey = CacheUniqueKey.create(cacheUniqueKey.getAClass(), appendKeyList);
	}

	public CacheUniqueKey getCacheUniqueKey() {
		return cacheUniqueKey;
	}

	protected IDataLifePredicate getLifePredicate() {
		return lifePredicate == null ? IDataLifePredicate.DEFAULT : lifePredicate;
	}

	public ICacheDBLifeInteract getCacheLifeInteract() {
		return cacheLifeInteract == null ? ICacheDBLifeInteract.DEFAULT : cacheLifeInteract;
	}

	protected DataSourceBuilder<K, V> newDataSourceBuilder(){
		ICacheSource<K, V> cacheSource = createCacheSource();
		DataSourceBuilder<K, V> dataSourceBuilder = new DataSourceBuilder<>(cacheSource);
		IConfig dataConfig = Configs.getInstance().getConfig("cache.data");
		return dataSourceBuilder.setDecorators(dataConfig.getList("decorators"));
	}

	@SuppressWarnings("unchecked")
	private ICacheSource<K, V> createCacheSource(){
		try {
			ICacheSource<K, V> cacheSource;
			ClassConfig classConfig = cacheUniqueKey.getClassConfig();
			CacheType cacheType = classConfig.getCacheType();
			if (cacheType.equals(CacheType.Redis)){
				cacheSource = changeIfDelayCacheSource(createCacheRedisSource());
			}
			else {
				if (cacheType.equals(CacheType.MongoDb)) {
					cacheSource = changeIfDelayCacheSource(createCacheMongoDBSource());
				}
				else {
					throw new CacheException("unexpected cache type:%s", cacheType.name());
				}
				if (classConfig.isRedisSupport()){
					cacheSource = new CacheComposeSource<>(createCacheRedisSource(), cacheSource, daoManager.getExecutor());
				}
			}
			return cacheSource;
		}
		catch (Throwable t) {
			throw new CacheException("%s", t, cacheUniqueKey.getAClass().getName());
		}
	}

	private CacheRedisSource<K, V> createCacheRedisSource(){
		ICacheRedisInteract cacheRedisInteract = daoManager.getCacheRedisInteract(cacheUniqueKey, getCacheLifeInteract());
		return new CacheRedisSource<>(cacheUniqueKey, secondaryBuilder, cacheRedisInteract);
	}

	private CacheMongoDBSource<K, V> createCacheMongoDBSource(){
		ICacheDBInteract cacheDBInteract = daoManager.getCacheDBInteract(cacheUniqueKey, getCacheLifeInteract());
		return new CacheMongoDBSource<>(cacheUniqueKey, secondaryBuilder, cacheDBInteract);
	}

	private ICacheSource<K, V> changeIfDelayCacheSource(ICacheSource<K, V> cacheSource){
		ClassConfig classConfig = cacheUniqueKey.getClassConfig();
		if (classConfig.delayUpdate){
			cacheSource = cacheSource.createDelayUpdateSource(daoManager.getExecutor());
		}
		return cacheSource;
	}
}
