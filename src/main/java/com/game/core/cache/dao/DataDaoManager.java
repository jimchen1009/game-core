package com.game.core.cache.dao;

import com.game.common.config.EvnCoreConfigs;
import com.game.common.config.IEvnConfig;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.data.IData;
import com.game.core.cache.key.IKeyValueBuilder;
import com.game.core.cache.source.executor.CacheExecutor;
import com.game.core.cache.source.executor.ICacheExecutor;
import com.game.core.cache.source.interact.CacheDBInteract;
import com.game.core.cache.source.interact.CacheRedisInteract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final Map<ICacheUniqueId, IDataCacheMapDao> mapDaoMap;
    private final Map<ICacheUniqueId, IDataCacheValueDao> valueDaoMap;
    private final Map<ICacheUniqueId, IDataCacheDao> cacheDaoMap;

    private final CacheDBInteract cacheDBInteract;
    private final CacheRedisInteract cacheRedisInteract;


    private DataDaoManager() {
        IEvnConfig executorConfig = EvnCoreConfigs.getConfig("cache.executor");
        this.executor = new CacheExecutor(executorConfig.getInt("threadCount"));
        this.mapDaoMap = new ConcurrentHashMap<>();
        this.valueDaoMap = new ConcurrentHashMap<>();
        this.cacheDaoMap = new ConcurrentHashMap<>();
        this.cacheDBInteract = new CacheDBInteract(this::handleCacheInteract, cacheDaoMap::keySet);
        this.cacheRedisInteract = new CacheRedisInteract(this::handleCacheInteract, cacheDaoMap::keySet);
    }

    public void flushAll(){
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<ICacheUniqueId, IDataCacheDao> entry : cacheDaoMap.entrySet()) {
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
        List<Map.Entry<ICacheUniqueId, IDataCacheDao>> entryList = cacheDaoMap.entrySet().stream().filter(entry -> entry.getKey().isAccountCache()).collect(Collectors.toList());
        if (entryList.isEmpty()){
            consumer.accept(true);
            return;
        }
        List<String> stringList = new ArrayList<>();
        CountDownLatch downLatch = new CountDownLatch(entryList.size());
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        for (Map.Entry<ICacheUniqueId, IDataCacheDao> entry : entryList) {
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

    <K, V extends IData<K>> DataMapDaoBuilder<K, V> newMapDaoBuilder(Class<V> aClass, IKeyValueBuilder<K> secondaryBuilder){
        DataMapDaoBuilder<K, V> builder = new DataMapDaoBuilder<>(aClass, secondaryBuilder);
        builder.setDaoManager(this);
        return builder;
    }

    <V extends IData<Long>> DataValueDaoBuilder<V> newValueDaoBuilder(Class<V> aClass){
        DataValueDaoBuilder<V> builder = new DataValueDaoBuilder<>(aClass);
        builder.setDaoManager(this);
        return builder;
    }

    ICacheExecutor getExecutor() {
        return executor;
    }

    CacheDBInteract getCacheDBInteract() {
        return cacheDBInteract;
    }

    CacheRedisInteract getCacheRedisInteract() {
        return cacheRedisInteract;
    }

    /**
     * 处理回调数据~
     * @param primaryKey
     * @param cacheDaoUnique
     */
    private void handleCacheInteract(long primaryKey, ICacheUniqueId cacheDaoUnique){
        IDataCacheMapDao cacheMapDao = mapDaoMap.get(cacheDaoUnique);
        if (cacheMapDao == null){
            IDataCacheValueDao cacheValueDao = valueDaoMap.get(cacheDaoUnique);
            if (cacheValueDao != null){
                cacheValueDao.get(primaryKey);
            }
        }
        else {
            cacheMapDao.getAll(primaryKey);
        }
    }

    @SuppressWarnings("unchecked")
    <V extends IData<Long>> IDataCacheValueDao<V> addCacheValueDao(IDataCacheValueDao<V> dataValueDao){
        ICacheUniqueId cacheUniqueId = dataValueDao.getCacheUniqueId();
        valueDaoMap.putIfAbsent(cacheUniqueId, dataValueDao);
        IDataCacheValueDao<V> onlyOneDao = valueDaoMap.get(cacheUniqueId);
        cacheDaoMap.putIfAbsent(cacheUniqueId, onlyOneDao);
        return onlyOneDao;
    }

    @SuppressWarnings("unchecked")
    <K, V extends IData<K>> IDataCacheMapDao<K, V> addCacheMapDao(IDataCacheMapDao<K, V> dataMapDao){
        ICacheUniqueId cacheUniqueId = dataMapDao.getCacheUniqueId();
        mapDaoMap.putIfAbsent(cacheUniqueId, dataMapDao);
        IDataCacheMapDao<K, V> onlyOneDao = mapDaoMap.get(cacheUniqueId);
        cacheDaoMap.putIfAbsent(cacheUniqueId, onlyOneDao);
        return onlyOneDao;
    }

    /**************************================================= 以上是内部方法 ==============================================****************************/
}
