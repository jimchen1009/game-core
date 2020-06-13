package com.game.cache.dao;

import com.game.cache.CacheDaoUnique;
import com.game.cache.CacheType;
import com.game.cache.ICacheDaoUnique;
import com.game.cache.data.DataSourceBuilder;
import com.game.cache.data.IData;
import com.game.cache.data.IDataLoadPredicate;
import com.game.cache.data.IDataSource;
import com.game.cache.data.map.DataMapContainer;
import com.game.cache.data.value.DataValueContainer;
import com.game.cache.exception.CacheException;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.key.KeyValueHelper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DataDaoManager {

    private static final Logger logger = LoggerFactory.getLogger(DataDaoManager.class);

    private static final DataDaoManager instance = new DataDaoManager();

    public static DataDaoManager getInstance() {
        return instance;
    }

    private final CacheType cacheType;
    private final ICacheExecutor executor;
    private final Map<ICacheDaoUnique, IDataMapDao> mapDaoMap;
    private final Map<ICacheDaoUnique, IDataValueDao> valueDaoMap;
    private final Map<ICacheDaoUnique, IDataCacheDao> cacheDaoMap;

    private final Map<String, ICacheSourceInteract> cacheName2SourceInteracts;


    public DataDaoManager() {
        this.cacheType = CacheType.valueOf(Configs.getInstance().getString("cache.type"));
        IConfig executorConfig = Configs.getInstance().getConfig("cache.executor");
        this.executor = new CacheExecutor(executorConfig.getInt("threadCount"));
        this.mapDaoMap = new ConcurrentHashMap<>();
        this.valueDaoMap = new ConcurrentHashMap<>();
        this.cacheDaoMap = new ConcurrentHashMap<>();
        this.cacheName2SourceInteracts = new ConcurrentHashMap<>();
    }

    public void addValueConverter(ValueConverter<?> convert){
        cacheType.getConvertMapper().add(convert);
    }

    public void initClass(Class<?> aClass){
        Objects.requireNonNull(ClassConfig.getConfig(aClass), aClass.getName());
    }

    public <K, V extends IData<K>> DataMapDaoBuilder<K, V> newDataMapDaoBuilder(Class<V> aClass, IKeyValueBuilder<K> secondaryBuilder){
        return new DataMapDaoBuilder<>(aClass, secondaryBuilder);
    }

    public <V extends IData<Long>> DataValueDaoBuilder<V> newDataValueDaoBuilder(Class<V> aClass){
        return new DataValueDaoBuilder<>(aClass);
    }

    public IDataCacheMapDao getDataCacheMapDao(ICacheDaoUnique cacheDaoUnique){
        return (IDataCacheMapDao)cacheDaoMap.get(cacheDaoUnique);
    }


    public IDataCacheValueDao getDataCacheValueDao(ICacheDaoUnique cacheDaoUnique){
        return (IDataCacheValueDao)cacheDaoMap.get(cacheDaoUnique);
    }
    
    public void flushAll(){
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<ICacheDaoUnique, IDataCacheDao> entry : cacheDaoMap.entrySet()) {
            try {
                if (entry.getValue().flushAll(currentTime)){

                }
                else {
                    logger.error("flushOne:{} failure.", entry.getKey());
                }
            }
            catch (Throwable t){
                logger.error("flushOne:{} error.", entry.getKey(), t);
            }
        }
    }

    public void flushUserAll(long primaryId, Consumer<Boolean> consumer){
        long currentTime = System.currentTimeMillis();
        List<Map.Entry<ICacheDaoUnique, IDataCacheDao>> entryList = cacheDaoMap.entrySet().stream().filter(entry -> entry.getKey().isUserCache()).collect(Collectors.toList());
        if (entryList.isEmpty()){
            consumer.accept(true);
            return;
        }
        List<String> stringList = new ArrayList<>();
        CountDownLatch downLatch = new CountDownLatch(entryList.size());
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        for (Map.Entry<ICacheDaoUnique, IDataCacheDao> entry : entryList) {
            entry.getValue().flushOne(primaryId, currentTime, success -> {
                downLatch.countDown();
                atomicBoolean.compareAndSet(false, success != null && success);
            });
        }
        try {
            downLatch.await();
            consumer.accept(atomicBoolean.get());
        }
        catch (InterruptedException e) {
            logger.error("flushUserAll:{} error.", primaryId, e);
            consumer.accept(false);
        }
    }

    public class DataMapDaoBuilder<K, V extends IData<K>>  extends DataDaoBuilder{

        public DataMapDaoBuilder(Class<V> aClass, IKeyValueBuilder<K> secondaryBuilder) {
            super(aClass, secondaryBuilder);
        }

        public DataMapDaoBuilder<K, V> setLoadPredicate(IDataLoadPredicate loadPredicate) {
            this.loadPredicate = loadPredicate;
            return this;
        }

        public DataMapDaoBuilder<K, V> setCacheLoginPredicate(ICacheLoginPredicate loginSharedLoad) {
            this.loginSharedLoad = loginSharedLoad;
            return this;
        }

        @SuppressWarnings("unchecked")
        public IDataMapDao<K, V> buildNoCache(){
            return mapDaoMap.computeIfAbsent(cacheDaoUnique, key -> {
                DataSourceBuilder<K, V> dataSourceBuilder = newDataSourceBuilder();
                IDataSource<K, V> dataSource = dataSourceBuilder.buildDirect();
                return new DataMapDao<>(dataSource);
            });
        }

        @SuppressWarnings("unchecked")
        public IDataMapDao<K, V> buildCache(){
            return (IDataMapDao<K, V>)cacheDaoMap.computeIfAbsent(cacheDaoUnique, key -> {
                DataSourceBuilder<K, V> dataSourceBuilder = newDataSourceBuilder();
                IDataSource<K, V> dataSource = dataSourceBuilder.buildDirect();
                DataMapContainer<K, V> container = new DataMapContainer<>(dataSourceBuilder.build(), getLoadPredicate());
                return new DataCacheMapDao<>(dataSource, container);
            });
        }
    }

    public class DataValueDaoBuilder<V extends IData<Long>>  extends DataDaoBuilder{

        public DataValueDaoBuilder(Class<V> aClass) {
            super(aClass, KeyValueHelper.LongBuilder);
        }

        public DataValueDaoBuilder<V> setLoadPredicate(IDataLoadPredicate loadPredicate) {
            this.loadPredicate = loadPredicate;
            return this;
        }

        public DataValueDaoBuilder<V> setCacheLoginPredicate(ICacheLoginPredicate loginSharedLoad) {
            this.loginSharedLoad = loginSharedLoad;
            return this;
        }

        @SuppressWarnings("unchecked")
        public IDataValueDao<V> buildNoCache(){
            return valueDaoMap.computeIfAbsent(cacheDaoUnique, key -> {
                DataSourceBuilder<Long, V> dataSourceBuilder = newDataSourceBuilder();
                IDataSource<Long, V> dataSource = dataSourceBuilder.buildDirect();
                return new DataValueDao<>(dataSource);
            });
        }

        @SuppressWarnings("unchecked")
        public IDataCacheValueDao<V> buildCache(){
            return (IDataCacheValueDao<V>)cacheDaoMap.computeIfAbsent(cacheDaoUnique, key -> {
                DataSourceBuilder<Long, V> dataSourceBuilder = newDataSourceBuilder();
                IDataSource<Long, V> dataSource = dataSourceBuilder.buildDirect();
                DataValueContainer<V> container = new DataValueContainer<>(dataSourceBuilder.build(), getLoadPredicate());
                return new DataCacheValueDao<>(dataSource, container);
            });
        }
    }


    public class DataDaoBuilder<K, V extends IData<K>> {
        protected CacheDaoUnique cacheDaoUnique;
        private final IKeyValueBuilder<K> secondaryBuilder;
        protected IDataLoadPredicate loadPredicate;
        protected ICacheLoginPredicate loginSharedLoad;

        private DataDaoBuilder(Class<V> aClass, IKeyValueBuilder<K> secondaryBuilder) {
            this.cacheDaoUnique = CacheDaoUnique.create(aClass);
            this.secondaryBuilder = secondaryBuilder;
        }

        public void setAppendKeyList(List<Map.Entry<String, Object>> appendKeyList) {
            cacheDaoUnique = CacheDaoUnique.create(cacheDaoUnique.getAClass(), appendKeyList);
        }

        protected IDataLoadPredicate getLoadPredicate() {
            if (loadPredicate == null){
                return new IDataLoadPredicate() {
                    @Override
                    public void onPredicateCacheLoaded(long primaryKey) {
                    }

                    @Override
                    public boolean predicateNoCache(long primaryKey) {
                        return false;
                    }
                };
            }
            return loadPredicate;
        }

        public ICacheLoginPredicate getLoginSharedLoad() {
            return loginSharedLoad == null ? new ICacheLoginPredicate() {

                @Override
                public boolean loginSharedLoadTable(long primaryKey, ICacheDaoUnique cacheDaoUnique) {
                    return false;
                }

                @Override
                public boolean loginSharedLoadRedis(long primaryKey, int redisSharedId) {
                    return false;
                }
            }: loginSharedLoad;
        }

        protected DataSourceBuilder<K, V> newDataSourceBuilder(){
            DataSourceBuilder<K, V> dataSourceBuilder = new DataSourceBuilder<>(createCacheSource());
            IConfig dataConfig = Configs.getInstance().getConfig("cache.data");
            return dataSourceBuilder.setDecorators(dataConfig.getList("decorators"))
                    .setConvertMapper(cacheType.getConvertMapper());
        }

        @SuppressWarnings("unchecked")
        private ICacheSource<K, V> createCacheSource(){
            try {
                ICacheSource<K, V> cacheSource;
                ClassConfig classConfig = cacheDaoUnique.getClassConfig();
                if (classConfig.isNoDbCache()){
                    //目前REDIS结构的数据，还没有延迟回写实现
                    cacheSource = new CacheRedisSource<>(cacheDaoUnique, secondaryBuilder);
                }
                else {
                    ICacheSourceInteract iCacheSourceInteract = cacheName2SourceInteracts.computeIfAbsent(classConfig.tableName, key -> new CacheInteraction(DataDaoManager.this, getLoginSharedLoad()));
                    if (cacheType.equals(CacheType.MongoDb)) {
                        cacheSource = new CacheMongoDBSource(cacheDaoUnique, secondaryBuilder, iCacheSourceInteract);
                    }
                    else {
                        throw new CacheException("unexpected cache type:%s", cacheType.name());
                    }
                    if (classConfig.delayUpdate){
                        cacheSource = cacheSource.createDelayUpdateSource(executor);
                    }
                    if (classConfig.enableRedis){
                        CacheRedisSource<K, V> redisSource = new CacheRedisSource<>(cacheDaoUnique, secondaryBuilder);
                        cacheSource = new CacheComposeSource<>(redisSource, cacheSource);
                    }
                }
                return cacheSource;
            } catch (Throwable t) {
                throw new CacheException("%s", t, cacheDaoUnique.getAClass().getName());
            }
        }
    }

}
