package com.game.cache.dao;

import com.game.cache.CacheUniqueKey;
import com.game.cache.ICacheUniqueKey;
import com.game.cache.config.ClassConfig;
import com.game.cache.data.IData;
import com.game.cache.key.IKeyValueBuilder;
import com.game.cache.source.executor.CacheExecutor;
import com.game.cache.source.executor.ICacheExecutor;
import com.game.cache.source.interact.CacheDBInteract;
import com.game.cache.source.interact.CacheRedisInteract;
import com.game.cache.source.interact.ICacheDBInteract;
import com.game.cache.source.interact.ICacheLifeInteract;
import com.game.cache.source.interact.ICacheRedisInteract;
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

    private final ICacheExecutor executor;
    private final Map<ICacheUniqueKey, IDataMapDao> mapDaoMap;
    private final Map<ICacheUniqueKey, IDataValueDao> valueDaoMap;
    private final Map<ICacheUniqueKey, IDataCacheDao> cacheDaoMap;

    private final Map<String, ICacheDBInteract> name2DBInteracts;
    private final Map<String, ICacheRedisInteract> name2RedisInteracts;


    public DataDaoManager() {
        IConfig executorConfig = Configs.getInstance().getConfig("cache.executor");
        this.executor = new CacheExecutor(executorConfig.getInt("threadCount"));
        this.mapDaoMap = new ConcurrentHashMap<>();
        this.valueDaoMap = new ConcurrentHashMap<>();
        this.cacheDaoMap = new ConcurrentHashMap<>();
        this.name2DBInteracts = new ConcurrentHashMap<>();
        this.name2RedisInteracts = new ConcurrentHashMap<>();
    }

    public void initClass(Class<?> aClass){
        Objects.requireNonNull(ClassConfig.getConfig(aClass), aClass.getName());
    }

    public <K, V extends IData<K>> DataMapDaoBuilder<K, V> newMapDaoBuilder(Class<V> aClass, IKeyValueBuilder<K> secondaryBuilder){
        DataMapDaoBuilder<K, V> builder = new DataMapDaoBuilder<>(aClass, secondaryBuilder);
        builder.setDaoManager(this);
        return builder;
    }

    public <V extends IData<Long>> DataValueDaoBuilder<V> newValueDaoBuilder(Class<V> aClass){
        DataValueDaoBuilder<V> builder = new DataValueDaoBuilder<>(aClass);
        builder.setDaoManager(this);
        return builder;
    }

    public void flushAll(){
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<ICacheUniqueKey, IDataCacheDao> entry : cacheDaoMap.entrySet()) {
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
        List<Map.Entry<ICacheUniqueKey, IDataCacheDao>> entryList = cacheDaoMap.entrySet().stream().filter(entry -> entry.getKey().isAccountCache()).collect(Collectors.toList());
        if (entryList.isEmpty()){
            consumer.accept(true);
            return;
        }
        List<String> stringList = new ArrayList<>();
        CountDownLatch downLatch = new CountDownLatch(entryList.size());
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        for (Map.Entry<ICacheUniqueKey, IDataCacheDao> entry : entryList) {
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

    /**************************================================= 以下是内部方法 ==============================================****************************/

    ICacheExecutor getExecutor() {
        return executor;
    }

    ICacheDBInteract getCacheDBInteract(CacheUniqueKey cacheUniqueKey, ICacheLifeInteract lifeInteract){
        return name2DBInteracts.computeIfAbsent(cacheUniqueKey.getName(), key -> new CacheDBInteract(lifeInteract, this::handleCacheInteract));
    }

    ICacheRedisInteract getCacheRedisInteract(CacheUniqueKey cacheUniqueKey, ICacheLifeInteract lifeInteract){
        return name2RedisInteracts.computeIfAbsent(cacheUniqueKey.getName(), key -> new CacheRedisInteract(lifeInteract, this::handleCacheInteract));
    }

    /**
     * 处理回调数据~
     * @param primaryKey
     * @param cacheDaoUnique
     */
    private void handleCacheInteract(long primaryKey, ICacheUniqueKey cacheDaoUnique){
        IDataCacheMapDao cacheMapDao = getDataCacheMapDao(cacheDaoUnique);
        if (cacheMapDao == null){
            IDataCacheValueDao cacheValueDao = getDataCacheValueDao(cacheDaoUnique);
            if (cacheValueDao != null){
                cacheValueDao.get(primaryKey);
            }
        }
        else {
            cacheMapDao.getAll(primaryKey);
        }
    }

    private IDataCacheMapDao getDataCacheMapDao(ICacheUniqueKey cacheDaoUnique){
        return (IDataCacheMapDao)mapDaoMap.get(cacheDaoUnique);
    }

    private IDataCacheValueDao getDataCacheValueDao(ICacheUniqueKey cacheDaoUnique){
        return (IDataCacheValueDao)valueDaoMap.get(cacheDaoUnique);
    }

    @SuppressWarnings("unchecked")
    <V extends IData<Long>> IDataValueDao<V> addValueDao(ICacheUniqueKey daoUnique, IDataValueDao<V> dataValueDao){
        valueDaoMap.putIfAbsent(daoUnique, dataValueDao);
        IDataValueDao<V> onlyOneDao = (IDataValueDao<V>) valueDaoMap.get(daoUnique);
        if (onlyOneDao instanceof IDataCacheDao) {
            cacheDaoMap.putIfAbsent(daoUnique, ((IDataCacheDao) onlyOneDao));
        }
        return onlyOneDao;
    }

    @SuppressWarnings("unchecked")
    <K, V extends IData<K>> IDataMapDao<K, V> addMapDao(ICacheUniqueKey daoUnique, IDataMapDao<K, V> dataMapDao){
        mapDaoMap.putIfAbsent(daoUnique, dataMapDao);
        IDataMapDao<K, V> onlyOneDao = mapDaoMap.get(daoUnique);
        if (onlyOneDao instanceof IDataCacheDao) {
            cacheDaoMap.putIfAbsent(daoUnique, ((IDataCacheDao) onlyOneDao));
        }
        return onlyOneDao;
    }

    /**************************================================= 以上是内部方法 ==============================================****************************/
}
