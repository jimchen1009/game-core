package com.game.core.cache.dao;

import com.game.core.cache.CacheType;
import com.game.core.cache.CacheUniqueId;
import com.game.core.cache.ClassConfig;
import com.game.core.cache.data.IData;
import com.game.core.cache.data.IDataLifePredicate;
import com.game.core.cache.exception.CacheException;
import com.game.core.cache.key.IKeyValueBuilder;
import com.game.core.cache.source.compose.CacheComposeSource;
import com.game.core.cache.source.executor.ICacheSource;
import com.game.core.cache.source.interact.ICacheDBInteract;
import com.game.core.cache.source.interact.ICacheDBLifeInteract;
import com.game.core.cache.source.interact.ICacheLifeInteract;
import com.game.core.cache.source.interact.ICacheRedisInteract;
import com.game.core.cache.source.mongodb.CacheMongoDBSource;
import com.game.core.cache.source.redis.CacheRedisSource;

public class DataDaoBuilder <K, V extends IData<K>> {

	protected final ClassConfig classConfig;
	protected final IKeyValueBuilder<K> secondaryBuilder;

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
		CacheUniqueId cacheUniqueId = new CacheUniqueId(classConfig);
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
