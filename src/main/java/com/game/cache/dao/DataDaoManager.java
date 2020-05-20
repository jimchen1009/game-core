package com.game.cache.dao;

import com.game.cache.CacheType;
import com.game.cache.data.IData;
import com.game.cache.exception.CacheException;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.ClassDescription;
import com.game.cache.mapper.ValueConverter;
import com.game.cache.source.executor.CacheExecutor;
import com.game.cache.source.executor.ICacheExecutor;
import com.game.cache.source.executor.ICacheSource;
import com.game.cache.source.mongodb.CacheDirectMongoDBSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataDaoManager {

    private static final DataDaoManager instance = new DataDaoManager();

    public static DataDaoManager getInstance() {
        return instance;
    }

    private final CacheType cacheType;
    private final ICacheExecutor executor;
    private final Map<String, IDataMapDao> mapDaoMap = new ConcurrentHashMap<>();
    private final Map<String, IDataCacheMapDao> cacheMapDaoMap = new ConcurrentHashMap<>();

    private final Map<String, IDataValueDao> valueDaoMap = new ConcurrentHashMap<>();
    private final Map<String, IDataCacheValueDao> cacheValueDaoMap = new ConcurrentHashMap<>();


    public DataDaoManager() {
        this.cacheType = CacheType.MongoDb;
        this.executor = new CacheExecutor(5);
    }

    public void addValueConverter(ValueConverter<?> convert){
        cacheType.getValueConvertMapper().add(convert);
    }

    /**
     * 非缓存MapDao
     * @param aClass
     * @param primaryBuilder
     * @param secondaryBuilder
     * @param <PK>
     * @param <K>
     * @param <V>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <PK, K, V extends IData<K>> IDataMapDao<PK, K, V> getMapDao(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder){
        return mapDaoMap.computeIfAbsent(aClass.getName(), key -> {
            ICacheSource<PK, K, V> cacheSource = createCacheSource(aClass, primaryBuilder, secondaryBuilder);
            return new DataMapDao<>(aClass, cacheType.getValueConvertMapper(), cacheSource);
        });
    }

    /**
     * 缓存的MapDao
     * @param aClass
     * @param primaryBuilder
     * @param secondaryBuilder
     * @param <PK>
     * @param <K>
     * @param <V>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <PK, K, V extends IData<K>> IDataCacheMapDao<PK, K, V> getCacheMapDao(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder){
        return cacheMapDaoMap.computeIfAbsent(aClass.getName(), key -> {
            ICacheSource<PK, K, V> cacheSource = createCacheSource(aClass, primaryBuilder, secondaryBuilder);
            return new DataCacheMapDao<>(aClass, cacheType.getValueConvertMapper(), cacheSource);
        });
    }

    /**
     * 非缓存的ValueDao
     * @param aClass
     * @param primaryBuilder
     * @param <PK>
     * @param <V>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <PK, V extends IData<PK>> IDataValueDao<PK, V> getValueDao(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder){
        return valueDaoMap.computeIfAbsent(aClass.getName(), key -> {
            ICacheSource<PK, PK, V> cacheSource = createCacheSource(aClass, primaryBuilder, primaryBuilder);
            return new DataValueDao<>(aClass, cacheType.getValueConvertMapper(), cacheSource);
        });
    }

    /**
     * 缓存的ValueDao
     * @param aClass
     * @param primaryBuilder
     * @param <PK>
     * @param <V>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <PK, V extends IData<PK>> IDataCacheValueDao<PK, V> getCacheValueDao(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder){
        return cacheValueDaoMap.computeIfAbsent(aClass.getName(), key -> {
            ICacheSource<PK, PK, V> cacheSource = createCacheSource(aClass, primaryBuilder, primaryBuilder);
            return new DataCacheValueDao<>(aClass, cacheType.getValueConvertMapper(), cacheSource);
        });
    }


    @SuppressWarnings("unchecked")
    private <PK, K, V extends IData<K>>  ICacheSource<PK, K, V> createCacheSource(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder){
        try {
            ICacheSource<PK, K, V> cacheSource;
            if (cacheType.equals(CacheType.MongoDb)) {
                cacheSource = new CacheDirectMongoDBSource(aClass, primaryBuilder, secondaryBuilder);
            }
            else {
                throw new CacheException("unexpected cache type:%s", cacheType.name());
            }
            boolean delayUpdate = ClassDescription.get(aClass).getCacheEntity().delayUpdate();
            if (delayUpdate) {
                cacheSource = cacheSource.createDelayUpdateSource(executor);
            }
            return cacheSource;
        } catch (Throwable t) {
            throw new CacheException("%s", t, aClass.getName());
        }
    }
}
