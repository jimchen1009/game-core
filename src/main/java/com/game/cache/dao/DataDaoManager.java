package com.game.cache.dao;

import com.game.cache.CacheInteraction;
import com.game.cache.CacheType;
import com.game.cache.data.DataSourceBuilder;
import com.game.cache.data.IData;
import com.game.cache.data.IDataLoadPredicate;
import com.game.cache.data.IDataSource;
import com.game.cache.data.map.DataMapContainer;
import com.game.cache.data.value.DataValueContainer;
import com.game.cache.exception.CacheException;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.ClassDescription;
import com.game.cache.mapper.ValueConverter;
import com.game.cache.source.executor.CacheExecutor;
import com.game.cache.source.executor.ICacheExecutor;
import com.game.cache.source.executor.ICacheSource;
import com.game.cache.source.mongodb.CacheDirectMongoDBSource;
import com.game.common.config.Configs;
import com.game.common.config.IConfig;

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
        this.cacheType = CacheType.valueOf(Configs.getInstance().getString("cache.type"));
        IConfig executorConfig = Configs.getInstance().getConfig("cache.executor");
        this.executor = new CacheExecutor(executorConfig.getInt("threadCount"));
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

        public DataMapDaoBuilder<PK, K, V> setLoadPredicate(IDataLoadPredicate<PK> loadPredicate) {
            this.loadPredicate = loadPredicate;
            return this;
        }

        @SuppressWarnings("unchecked")
        public IDataMapDao<PK, K, V> buildNoCache(){
            return mapDaoMap.computeIfAbsent(aClass.getName(), key -> {
                DataSourceBuilder<PK, K, V> dataSourceBuilder = newDataSourceBuilder();
                IDataSource<PK, K, V> dataSource = dataSourceBuilder.buildDirect();
                return new DataMapDao<>(dataSource);
            });
        }

        @SuppressWarnings("unchecked")
        public IDataMapDao<PK, K, V> buildCache(){
            return cacheMapDaoMap.computeIfAbsent(aClass.getName(), key -> {
                DataSourceBuilder<PK, K, V> dataSourceBuilder = newDataSourceBuilder();
                IDataSource<PK, K, V> dataSource = dataSourceBuilder.buildDirect();
                DataMapContainer<PK, K, V> container = new DataMapContainer<>(dataSourceBuilder.build(), getLoadPredicate());
                return new DataCacheMapDao<>(dataSource, container);
            });
        }
    }

    public class DataValueDaoBuilder<PK, V extends IData<PK>>  extends DataDaoBuilder{

        public DataValueDaoBuilder(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder) {
            super(aClass, primaryBuilder, primaryBuilder);
        }

        public DataValueDaoBuilder<PK, V> setLoadPredicate(IDataLoadPredicate<PK> loadPredicate) {
            this.loadPredicate = loadPredicate;
            return this;
        }

        @SuppressWarnings("unchecked")
        public IDataValueDao<PK, V> buildNoCache(){
            return valueDaoMap.computeIfAbsent(aClass.getName(), key -> {
                DataSourceBuilder<PK, PK, V> dataSourceBuilder = newDataSourceBuilder();
                IDataSource<PK, PK, V> dataSource = dataSourceBuilder.buildDirect();
                return new DataValueDao<>(dataSource);
            });
        }

        @SuppressWarnings("unchecked")
        public IDataCacheValueDao<PK, V> buildCache(){
            return cacheValueDaoMap.computeIfAbsent(aClass.getName(), key -> {
                DataSourceBuilder<PK, PK, V> dataSourceBuilder = newDataSourceBuilder();
                IDataSource<PK, PK, V> dataSource = dataSourceBuilder.buildDirect();
                DataValueContainer<PK, V> container = new DataValueContainer<>(dataSourceBuilder.build(), getLoadPredicate());
                return new DataCacheValueDao<>(dataSource, container);
            });
        }
    }


    public class DataDaoBuilder<PK, K, V extends IData<K>> {
        protected final Class<V> aClass;
        private final IKeyValueBuilder<PK> primaryBuilder;
        private final IKeyValueBuilder<K> secondaryBuilder;
        protected IDataLoadPredicate<PK> loadPredicate;

        private DataDaoBuilder(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder) {
            this.aClass = aClass;
            this.primaryBuilder = primaryBuilder;
            this.secondaryBuilder = secondaryBuilder;
        }

        protected IDataLoadPredicate<PK> getLoadPredicate() {
            if (loadPredicate == null){
                return new IDataLoadPredicate<PK>() {
                    @Override
                    public void onPredicateLoaded(PK primaryKey) {
                    }

                    @Override
                    public boolean predicateFirstTime(PK primaryKey) {
                        return false;
                    }
                };
            }
            return loadPredicate;
        }

        protected DataSourceBuilder<PK, K, V> newDataSourceBuilder(){
            DataSourceBuilder<PK, K, V> dataSourceBuilder = new DataSourceBuilder<>(aClass, createCacheSource());
            IConfig dataConfig = Configs.getInstance().getConfig("cache.data");
            return dataSourceBuilder.setDecorators(dataConfig.getList("decorators"))
                    .setConvertMapper(cacheType.getValueConvertMapper());
        }

        @SuppressWarnings("unchecked")
        private ICacheSource<PK, K, V> createCacheSource(){
            try {
                ICacheSource<PK, K, V> cacheSource;
                if (cacheType.equals(CacheType.MongoDb)) {
                    cacheSource = new CacheDirectMongoDBSource(aClass, primaryBuilder, secondaryBuilder);
                }
                else {
                    throw new CacheException("unexpected cache type:%s", cacheType.name());
                }
                if (ClassDescription.get(aClass).getCacheClass().delayUpdate()){
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
