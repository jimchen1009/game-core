package com.game.cache.dao;

import com.game.cache.CacheType;
import com.game.cache.data.IData;
import com.game.cache.exception.CacheException;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.ClassDescription;
import com.game.cache.mapper.ValueConverter;
import com.game.cache.mapper.annotation.CacheEntity;
import com.game.cache.source.executor.CacheExecutor;
import com.game.cache.source.executor.ICacheExecutor;
import com.game.cache.source.executor.ICacheSource;
import com.game.cache.source.mongodb.CacheDirectMongoDBSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    private final Map<String, ClassDescription> classMap = new ConcurrentHashMap<>();


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
            ICacheSource<PK, K, V> cacheSource = createCacheSource(aClass, primaryBuilder, secondaryBuilder, true);
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
            ICacheSource<PK, K, V> cacheSource = createCacheSource(aClass, primaryBuilder, secondaryBuilder, false);
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
            ICacheSource<PK, PK, V> cacheSource = createCacheSource(aClass, primaryBuilder, primaryBuilder, true);
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
            ICacheSource<PK, PK, V> cacheSource = createCacheSource(aClass, primaryBuilder, primaryBuilder, false);
            return new DataCacheValueDao<>(aClass, cacheType.getValueConvertMapper(), cacheSource);
        });
    }


    @SuppressWarnings("unchecked")
    private <PK, K, V extends IData<K>>  ICacheSource<PK, K, V> createCacheSource(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder, boolean directUpdate){
        try {
            ICacheSource<PK, K, V> cacheSource;
            if (cacheType.equals(CacheType.MongoDb)) {
                cacheSource = new CacheDirectMongoDBSource(aClass, primaryBuilder, secondaryBuilder);
            }
            else {
                throw new CacheException("unexpected cache type:%s", cacheType.name());
            }
            if (!directUpdate && ClassDescription.get(aClass).getCacheEntity().delayUpdate()){
                cacheSource = cacheSource.createDelayUpdateSource(executor);
            }
            checkClassCacheEntity(aClass);
            return cacheSource;
        } catch (Throwable t) {
            throw new CacheException("%s", t, aClass.getName());
        }
    }

    private void checkClassCacheEntity(Class<?> aClass){
        if (classMap.containsKey(aClass.getName())){
            return;
        }

        classMap.put(aClass.getName(), ClassDescription.get(aClass));
        Map<String, List<CacheEntity>> address2CacheEntities = new ConcurrentHashMap<>();
        for (ClassDescription description : classMap.values()) {
            CacheEntity cacheEntity = description.getCacheEntity();
            List<CacheEntity> cacheEntityList = address2CacheEntities.computeIfAbsent(cacheEntity.addressName(), key -> new ArrayList<>());
            cacheEntityList.add(cacheEntity);
        }
        for (Map.Entry<String, List<CacheEntity>> entry : address2CacheEntities.entrySet()) {
            List<CacheEntity> cacheEntityList = entry.getValue();
            cacheEntityList.sort(Comparator.comparingInt(CacheEntity::primaryId));
            if (cacheEntityList.size() == 1){
                continue;
            }
            List<Integer> primaryIds = cacheEntityList.stream().map(CacheEntity::primaryId).collect(Collectors.toList());
            for (int i = 1; i < primaryIds.size(); i++) {
                if (primaryIds.get(i) != primaryIds.get(i - 1)){
                    continue;
                }
                throw new CacheException("address:%s primaryId:%s", entry.getKey(), primaryIds);
            }
        }
    }
}
