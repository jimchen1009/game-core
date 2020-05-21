package com.game.cache.dao;

import com.game.cache.CacheInteraction;
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
    private final CacheInteraction cacheInteraction;
    private final Map<String, IDataMapDao> mapDaoMap;
    private final Map<String, IDataCacheMapDao> cacheMapDaoMap;
    private final Map<String, IDataValueDao> valueDaoMap;
    private final Map<String, IDataCacheValueDao> cacheValueDaoMap;


    public DataDaoManager() {
        this.cacheType = CacheType.MongoDb;
        this.executor = new CacheExecutor(5);
        this.cacheInteraction = new CacheInteraction();
        this.mapDaoMap = new ConcurrentHashMap<>();
        this.cacheMapDaoMap = new ConcurrentHashMap<>();
        this.valueDaoMap = new ConcurrentHashMap<>();
        this.cacheValueDaoMap = new ConcurrentHashMap<>();
    }

    public void addValueConverter(ValueConverter<?> convert){
        cacheType.getValueConvertMapper().add(convert);
    }

    public void initClass(Class<?> aClass){
        cacheInteraction.addClass(aClass);
    }

    public <PK, K, V extends IData<K>> DataMapDaoBuilder<PK, K, V> newDataMapDaoBuilder(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder){
        return new DataMapDaoBuilder<>(aClass, primaryBuilder, secondaryBuilder);
    }

    public <PK, V extends IData<PK>> DataValueDaoBuilder<PK, V> newDataValueDaoBuilder(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder){
        return new DataValueDaoBuilder<>(aClass, primaryBuilder);
    }


    public class DataMapDaoBuilder<PK, K, V extends IData<K>>  extends DataDaoBuilder{

        public DataMapDaoBuilder(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder) {
            super(aClass, primaryBuilder, secondaryBuilder);
        }


        @SuppressWarnings("unchecked")
        public IDataMapDao<PK, K, V> build(){
            return mapDaoMap.computeIfAbsent(aClass.getName(), key -> {
                ICacheSource<PK, K, V> cacheSource = createCacheSource(true);
                return new DataMapDao<>(aClass, cacheType.getValueConvertMapper(), cacheSource);
            });
        }

        @SuppressWarnings("unchecked")
        public IDataMapDao<PK, K, V> buildCache(){
            return cacheMapDaoMap.computeIfAbsent(aClass.getName(), key -> {
                ICacheSource<PK, K, V> cacheSource = createCacheSource(false);
                return new DataCacheMapDao<>(aClass, cacheType.getValueConvertMapper(), cacheSource);
            });
        }
    }

    public class DataValueDaoBuilder<PK, V extends IData<PK>>  extends DataDaoBuilder{

        public DataValueDaoBuilder(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder) {
            super(aClass, primaryBuilder, primaryBuilder);
        }

        public DataValueDaoBuilder<PK, V> setDirectUpdate() {
            return this;
        }

        @SuppressWarnings("unchecked")
        public IDataValueDao<PK, V> build(){
            return valueDaoMap.computeIfAbsent(aClass.getName(), key -> {
                ICacheSource<PK, PK, V> cacheSource = createCacheSource(true);
                return new DataValueDao<>(aClass, cacheType.getValueConvertMapper(), cacheSource);
            });
        }

        @SuppressWarnings("unchecked")
        public IDataCacheValueDao<PK, V> buildCache(){
            return cacheValueDaoMap.computeIfAbsent(aClass.getName(), key -> {
                ICacheSource<PK, PK, V> cacheSource = createCacheSource(false);
                return new DataCacheValueDao<>(aClass, cacheType.getValueConvertMapper(), cacheSource);
            });
        }
    }


    private class DataDaoBuilder<PK, K, V extends IData<K>> {
        final Class<V> aClass;
        IKeyValueBuilder<PK> primaryBuilder;
        IKeyValueBuilder<K> secondaryBuilder;

        public DataDaoBuilder(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder) {
            this.aClass = aClass;
            this.primaryBuilder = primaryBuilder;
            this.secondaryBuilder = secondaryBuilder;
        }

        @SuppressWarnings("unchecked")
        protected <PK, K, V extends IData<K>>  ICacheSource<PK, K, V> createCacheSource(boolean directUpdate){
            try {
                ICacheSource<PK, K, V> cacheSource;
                if (cacheType.equals(CacheType.MongoDb)) {
                    cacheSource = new CacheDirectMongoDBSource(aClass, primaryBuilder, secondaryBuilder);
                }
                else {
                    throw new CacheException("unexpected cache type:%s", cacheType.name());
                }
                if (!directUpdate && ClassDescription.get(aClass).getCacheClass().delayUpdate()){
                    cacheSource = cacheSource.createDelayUpdateSource(executor);
                }
                cacheInteraction.addClass(aClass);
                return cacheSource;
            } catch (Throwable t) {
                throw new CacheException("%s", t, aClass.getName());
            }
        }
    }
}
