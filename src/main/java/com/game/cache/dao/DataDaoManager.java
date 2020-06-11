package com.game.cache.dao;

import com.game.cache.CacheType;
import com.game.cache.data.DataSourceBuilder;
import com.game.cache.data.IData;
import com.game.cache.data.IDataLoadPredicate;
import com.game.cache.data.IDataSource;
import com.game.cache.data.map.DataMapContainer;
import com.game.cache.data.value.DataValueContainer;
import com.game.cache.exception.CacheException;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.mapper.ClassConfig;
import com.game.cache.mapper.ValueConverter;
import com.game.cache.source.ICacheLoginPredicate;
import com.game.cache.source.ICacheSourceInteract;
import com.game.cache.source.compose.CacheComposeSource;
import com.game.cache.source.executor.CacheExecutor;
import com.game.cache.source.executor.ICacheExecutor;
import com.game.cache.source.executor.ICacheSource;
import com.game.cache.source.mongodb.CacheMongoDBSource;
import com.game.cache.source.redis.CacheRedisSource;
import com.game.common.config.Configs;
import com.game.common.config.IConfig;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DataDaoManager {

    private static final DataDaoManager instance = new DataDaoManager();

    public static DataDaoManager getInstance() {
        return instance;
    }

    private final CacheType cacheType;
    private final ICacheExecutor executor;
    private final Map<String, IDataMapDao> mapDaoMap;
    private final Map<String, IDataCacheMapDao> cacheMapDaoMap;
    private final Map<String, IDataValueDao> valueDaoMap;
    private final Map<String, IDataCacheValueDao> cacheValueDaoMap;

    private final Map<String, ICacheSourceInteract> cacheName2SourceInteracts;


    public DataDaoManager() {
        this.cacheType = CacheType.valueOf(Configs.getInstance().getString("cache.type"));
        IConfig executorConfig = Configs.getInstance().getConfig("cache.executor");
        this.executor = new CacheExecutor(executorConfig.getInt("threadCount"));
        this.mapDaoMap = new ConcurrentHashMap<>();
        this.cacheMapDaoMap = new ConcurrentHashMap<>();
        this.valueDaoMap = new ConcurrentHashMap<>();
        this.cacheValueDaoMap = new ConcurrentHashMap<>();
        this.cacheName2SourceInteracts = new ConcurrentHashMap<>();
    }

    public void addValueConverter(ValueConverter<?> convert){
        cacheType.getConvertMapper().add(convert);
    }

    public void initClass(Class<?> aClass){
        Objects.requireNonNull(ClassConfig.getConfig(aClass), aClass.getName());
    }

    public <PK, K, V extends IData<K>> DataMapDaoBuilder<PK, K, V> newDataMapDaoBuilder(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder){
        return new DataMapDaoBuilder<>(aClass, primaryBuilder, secondaryBuilder);
    }

    public <PK, V extends IData<PK>> DataValueDaoBuilder<PK, V> newDataValueDaoBuilder(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder){
        return new DataValueDaoBuilder<>(aClass, primaryBuilder);
    }

    public IDataCacheMapDao getDataCacheMapDao(Class<?> aClass){
        return getDataCacheMapDao(aClass.getName());
    }

    public IDataCacheMapDao getDataCacheMapDao(String className){
        return cacheMapDaoMap.get(className);
    }

    public Collection<IDataCacheMapDao> getAllDataCacheMapDao(){
        return cacheMapDaoMap.values();
    }


    public IDataCacheValueDao getDataCacheValueDao(Class<?> aClass){
        return getDataCacheValueDao(aClass.getName());
    }

    public IDataCacheValueDao getDataCacheValueDao(String className){
        return cacheValueDaoMap.get(className);
    }

    public Collection<IDataCacheValueDao> getAllDataCacheValueDao(){
        return cacheValueDaoMap.values();
    }

    public class DataMapDaoBuilder<PK, K, V extends IData<K>>  extends DataDaoBuilder{

        public DataMapDaoBuilder(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder) {
            super(aClass, primaryBuilder, secondaryBuilder);
        }

        public DataMapDaoBuilder<PK, K, V> setLoadPredicate(IDataLoadPredicate<PK> loadPredicate) {
            this.loadPredicate = loadPredicate;
            return this;
        }

        public DataMapDaoBuilder<PK, K, V> setCacheLoginPredicate(ICacheLoginPredicate<PK> loginSharedLoad) {
            this.loginSharedLoad = loginSharedLoad;
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

        public DataValueDaoBuilder<PK, V> setCacheLoginPredicate(ICacheLoginPredicate<PK> loginSharedLoad) {
            this.loginSharedLoad = loginSharedLoad;
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
        protected ICacheLoginPredicate<PK> loginSharedLoad;

        private DataDaoBuilder(Class<V> aClass, IKeyValueBuilder<PK> primaryBuilder, IKeyValueBuilder<K> secondaryBuilder) {
            this.aClass = aClass;
            this.primaryBuilder = primaryBuilder;
            this.secondaryBuilder = secondaryBuilder;
        }

        protected IDataLoadPredicate<PK> getLoadPredicate() {
            if (loadPredicate == null){
                return new IDataLoadPredicate<PK>() {
                    @Override
                    public void onPredicateCacheLoaded(PK primaryKey) {
                    }

                    @Override
                    public boolean predicateNoCache(PK primaryKey) {
                        return false;
                    }
                };
            }
            return loadPredicate;
        }

        public ICacheLoginPredicate<PK> getLoginSharedLoad() {
            return loginSharedLoad == null ? new ICacheLoginPredicate<PK>() {
                @Override
                public boolean loginSharedLoadTable(PK primaryKey, String tableName) {
                    return false;
                }

                @Override
                public boolean loginSharedLoadRedis(PK primaryKey, int redisSharedId) {
                    return false;
                }
            }: loginSharedLoad;
        }

        protected DataSourceBuilder<PK, K, V> newDataSourceBuilder(){
            DataSourceBuilder<PK, K, V> dataSourceBuilder = new DataSourceBuilder<>(aClass, createCacheSource());
            IConfig dataConfig = Configs.getInstance().getConfig("cache.data");
            return dataSourceBuilder.setDecorators(dataConfig.getList("decorators"))
                    .setConvertMapper(cacheType.getConvertMapper());
        }

        @SuppressWarnings("unchecked")
        private ICacheSource<PK, K, V> createCacheSource(){
            try {
                ICacheSource<PK, K, V> cacheSource;
                ClassConfig classConfig = ClassConfig.getConfig(aClass);
                if (classConfig.isNoDbCache()){
                    //目前REDIS结构的数据，还没有延迟回写实现
                    cacheSource = new CacheRedisSource<>(aClass, primaryBuilder, secondaryBuilder);
                }
                else {
                    ICacheSourceInteract<PK> iCacheSourceInteract = cacheName2SourceInteracts.computeIfAbsent(classConfig.tableName, key -> new CacheInteraction(DataDaoManager.this, getLoginSharedLoad()));
                    if (cacheType.equals(CacheType.MongoDb)) {
                        cacheSource = new CacheMongoDBSource(aClass, primaryBuilder, secondaryBuilder, iCacheSourceInteract);
                    }
                    else {
                        throw new CacheException("unexpected cache type:%s", cacheType.name());
                    }
                    if (classConfig.delayUpdate){
                        cacheSource = cacheSource.createDelayUpdateSource(executor);
                    }
                    if (classConfig.enableRedis){
                        CacheRedisSource<PK, K, V> redisSource = new CacheRedisSource<>(aClass, primaryBuilder, secondaryBuilder);
                        cacheSource = new CacheComposeSource<>(redisSource, cacheSource);
                    }
                }
                return cacheSource;
            } catch (Throwable t) {
                throw new CacheException("%s", t, aClass.getName());
            }
        }
    }

}
