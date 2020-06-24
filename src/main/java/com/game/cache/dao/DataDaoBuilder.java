package com.game.cache.dao;

import com.game.cache.CacheType;
import com.game.cache.CacheUniqueId;
import com.game.cache.ClassConfig;
import com.game.cache.data.IData;
import com.game.cache.data.IDataLifePredicate;
import com.game.cache.exception.CacheException;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.source.compose.CacheComposeSource;
import com.game.cache.source.executor.ICacheSource;
import com.game.cache.source.interact.ICacheDBInteract;
import com.game.cache.source.interact.ICacheDBLifeInteract;
import com.game.cache.source.interact.ICacheLifeInteract;
import com.game.cache.source.interact.ICacheRedisInteract;
import com.game.cache.source.mongodb.CacheMongoDBSource;
import com.game.cache.source.redis.CacheRedisSource;

public class DataDaoBuilder <K, V extends IData<K>> {

	protected final ClassConfig classConfig;
	protected final IKeyValueBuilder<K> secondaryBuilder;

	protected String primaryAddKeyParams = "";
	protected IDataLifePredicate lifePredicate = IDataLifePredicate.DEFAULT;
	protected ICacheLifeInteract cacheLifeInteract = ICacheDBLifeInteract.DEFAULT;

	protected DataDaoManager daoManager;

	protected DataDaoBuilder(Class<V> aClass, IKeyValueBuilder<K> secondaryBuilder) {
		this.classConfig = new ClassConfig(aClass);
		this.secondaryBuilder = secondaryBuilder;
	}

	void setDaoManager(DataDaoManager daoManager) {
		this.daoManager = daoManager;
	}


	public void setPrimaryAddKeyParams(String primaryAddKeyParams) {
		this.primaryAddKeyParams = primaryAddKeyParams;
	}

	public void setLifePredicate(IDataLifePredicate lifePredicate) {
		this.lifePredicate = lifePredicate;
	}

	public void setCacheLoginPredicate(ICacheLifeInteract cacheLifeInteract) {
		this.cacheLifeInteract = cacheLifeInteract;
	}

	public ClassConfig getClassConfig() {
		return classConfig;
	}

	protected ICacheSource<K, V> createCacheSource(){
		return createCacheSource(classConfig);
	}

	protected ICacheSource<K, V> createCacheSource(ClassConfig classConfig){
		CacheUniqueId cacheUniqueId = new CacheUniqueId(classConfig, primaryAddKeyParams.trim());
		try {
			ICacheSource<K, V> cacheSource;
			CacheType cacheType = cacheUniqueId.getCacheType();
			if (cacheType.equals(CacheType.Redis)){
				cacheSource = changeIfDelayCacheSource(createCacheRedisSource(cacheUniqueId));
			}
			else {
				if (cacheType.equals(CacheType.MongoDb)) {
					cacheSource = changeIfDelayCacheSource(createCacheMongoDBSource(cacheUniqueId));
				}
				else {
					throw new CacheException("unexpected cache type:%s", cacheType.name());
				}
				if (cacheUniqueId.isRedisSupport()){
					cacheSource = new CacheComposeSource<>(createCacheRedisSource(cacheUniqueId), cacheSource, daoManager.getExecutor());
				}
			}
			return cacheSource;
		}
		catch (Throwable t) {
			throw new CacheException("%s", t, cacheUniqueId.getAClass().getName());
		}
	}

	private CacheRedisSource<K, V> createCacheRedisSource(CacheUniqueId cacheUniqueId){
		ICacheRedisInteract cacheRedisInteract = daoManager.getCacheRedisInteract();
		return new CacheRedisSource<>(cacheUniqueId, secondaryBuilder, cacheRedisInteract);
	}

	private CacheMongoDBSource<K, V> createCacheMongoDBSource(CacheUniqueId cacheUniqueId){
		ICacheDBInteract cacheDBInteract = daoManager.getCacheDBInteract();
		return new CacheMongoDBSource<>(cacheUniqueId, secondaryBuilder, cacheDBInteract);
	}

	private ICacheSource<K, V> changeIfDelayCacheSource(ICacheSource<K, V> cacheSource){
		if (cacheSource.getCacheUniqueId().isDelayUpdate()){
			cacheSource = cacheSource.createDelayUpdateSource(daoManager.getExecutor());
		}
		return cacheSource;
	}
}
