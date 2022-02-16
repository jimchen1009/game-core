package com.game.core.cache.dao;

import com.game.core.cache.CacheKeyValue;
import com.game.core.cache.CacheType;
import com.game.core.cache.CacheUniqueId;
import com.game.core.cache.ClassConfig;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.data.IData;
import com.game.core.cache.data.IDataLifePredicate;
import com.game.core.cache.exception.CacheException;
import com.game.core.cache.key.IKeyValueBuilder;
import com.game.core.cache.source.ICacheDelaySource;
import com.game.core.cache.source.compose.RedisDbSource;
import com.game.core.cache.source.executor.ICacheExecutor;
import com.game.core.cache.source.executor.ICacheSource;
import com.game.core.cache.source.redis.ICacheRedisSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DataDaoBuilder<K, V extends IData<K>> {

	protected final ClassConfig classConfig;
	protected final IKeyValueBuilder<K> secondaryBuilder;
	private final List<CacheKeyValue> additionalKeValueList;

	protected IDataLifePredicate lifePredicate = IDataLifePredicate.DEFAULT;

	protected DataDaoManager daoManager;

	protected DataDaoBuilder(Class<V> aClass, IKeyValueBuilder<K> secondaryBuilder) {
		this.classConfig = new ClassConfig(aClass);
		this.secondaryBuilder = secondaryBuilder;
		this.additionalKeValueList = new ArrayList<>();
	}

	public DataDaoBuilder<K, V> setLifePredicate(IDataLifePredicate lifePredicate) {
		this.lifePredicate = lifePredicate;
		return this;
	}

	public ClassConfig getClassConfig() {
		return classConfig;
	}

	public void addAdditionalKeyValue(CacheKeyValue cacheKeyValue){
		additionalKeValueList.add(cacheKeyValue);
	}

	void setDaoManager(DataDaoManager daoManager) {
		this.daoManager = daoManager;
	}

	ICacheSource<K, V> createCacheSource(){
		return createCacheSource(classConfig);
	}

	ICacheSource<K, V> createCacheSource(ClassConfig classConfig){
		CacheUniqueId cacheUniqueId = new CacheUniqueId(classConfig, additionalKeValueList);
		try {
			ICacheSource<K, V> cacheSource;
			cacheSource = changeIfDelayCacheSource(createCacheSource(cacheUniqueId, cacheUniqueId.getCacheType()));
			if (cacheUniqueId.isRedisSupport() && (cacheSource instanceof ICacheDelaySource)){
				ICacheRedisSource<K, V> redisSource = (ICacheRedisSource<K, V>)createCacheSource(cacheUniqueId, CacheType.Redis);
				cacheSource = new RedisDbSource<>(redisSource, (ICacheDelaySource<K, V>)cacheSource);
			}
			return cacheSource;
		}
		catch (Throwable t) {
			throw new CacheException("%s", t, cacheUniqueId.getAClass().getName());
		}
	}

	@SuppressWarnings("unchecked")
	private ICacheSource<K, V> createCacheSource(CacheUniqueId cacheUniqueId, CacheType cacheType) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		Class<? extends ICacheSource> cacheClass = cacheType.getCacheClass();
		Constructor<? extends ICacheSource> constructor = cacheClass.getConstructor(ICacheUniqueId.class, IKeyValueBuilder.class, ICacheExecutor.class);
		ICacheSource<K, V> newInstance = (ICacheSource<K, V>) constructor.newInstance(cacheUniqueId, secondaryBuilder, daoManager.getExecutor());
		return Objects.requireNonNull(newInstance);
	}

	private ICacheSource<K, V> changeIfDelayCacheSource(ICacheSource<K, V> cacheSource){
		if (cacheSource.getCacheUniqueId().isDelayUpdate()){
			cacheSource = cacheSource.createDelayUpdateSource();
		}
		return cacheSource;
	}
}
