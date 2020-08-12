package com.game.core.cache.source;

import com.game.common.config.EvnCoreConfigs;
import com.game.core.cache.CacheInformation;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.data.DataCollection;
import com.game.core.cache.data.IData;
import com.game.core.cache.exception.CacheException;
import com.game.core.cache.mapper.IClassConverter;
import com.game.core.cache.source.executor.CacheCallable;
import com.game.core.cache.source.executor.CacheRunnable;
import com.game.core.cache.source.executor.ICacheExecutor;
import com.game.core.cache.source.executor.ICacheFuture;
import com.game.core.cache.source.executor.ICacheSource;
import com.game.common.lock.LockKey;
import com.game.common.lock.LockUtil;
import com.mongodb.Function;
import jodd.util.ThreadUtil;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class CacheDelaySource<K, V extends IData<K>> implements ICacheDelaySource<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(CacheDelaySource.class);

    public static final Map<String, Object> EMPTY = Collections.emptyMap();

    private final CacheDbSource<K, V> cacheSource;
    private final Map<Long, PrimaryDelayCache<K, V>> primaryCacheMap;
    private final ICacheExecutor executor;
    private List<Consumer<PrimaryDelayCache<K, V>>> flushCallbacks;

    public CacheDelaySource(CacheDbSource<K, V> cacheSource, ICacheExecutor executor) {
        this.cacheSource = cacheSource;
        this.primaryCacheMap = new ConcurrentHashMap<>();
        this.executor = executor;
        this.flushCallbacks = new ArrayList<>();
        //初始化~
        CacheRunnable cacheRunnable = new CacheRunnable(getScheduleName(), this::onScheduleAll);
        long randomValue = RandomUtils.nextLong(1000, 2000);
        long initialDelay = randomValue * 50 / 50;    //取100ms的整数倍
        executor.scheduleAtFixedRate(cacheRunnable, initialDelay, 500L, TimeUnit.MILLISECONDS);
    }

    @Override
    public LockKey getLockKey(long primaryKey) {
        return cacheSource.getLockKey(primaryKey);
    }

    @Override
    public V get(long primaryKey, K secondaryKey) {
        PrimaryDelayCache<K, V> primaryCache = primaryCacheMap.get(primaryKey);
        if (primaryCache == null) {
            return cacheSource.get(primaryKey, secondaryKey);
        } else {
            return cacheSource.get(primaryKey, secondaryKey);
        }
    }

    @Override
    public List<V> getAll(long primaryKey) {
        List<V> dataValueList = cacheSource.getAll(primaryKey);
        return replaceAllDataValueList(primaryKey, dataValueList);
    }

    @Override
    public DataCollection<K, V> getCollection(long primaryKey) {
        DataCollection<K, V> collection = cacheSource.getCollection(primaryKey);
        List<V> valueList = replaceAllDataValueList(primaryKey, collection.getDataList());
        return new DataCollection<>(valueList, collection.getCacheInformation());
    }

    @Override
    public boolean replaceOne(long primaryKey, V value) {
        V cloneValue = cloneValue(value);
        PrimaryDelayCache<K, V> primaryCache = primaryCacheMap.computeIfAbsent(primaryKey, PrimaryDelayCache::new);
        KeyDataValue<K, V> keyDataValue = KeyDataValue.createCache(cloneValue.secondaryKey(), cloneValue);
        primaryCache.add(keyDataValue);	//直接替换还有BUG，因为里面的标记会被覆盖【暂时用全量覆盖掉】
        return true;
    }

    @Override
    public boolean replaceBatch(long primaryKey, Collection<V> values) {
        PrimaryDelayCache<K, V> primaryCache = primaryCacheMap.computeIfAbsent(primaryKey, PrimaryDelayCache::new);
        for (V value : values) {
            primaryCache.add(KeyDataValue.createCache(value.secondaryKey(), cloneValue(value)));
        }
        return true;
    }

    @Override
    public Class<V> getAClass() {
        return cacheSource.getAClass();
    }

    @Override
    public ICacheUniqueId getCacheUniqueId() {
        return cacheSource.getCacheUniqueId();
    }

    @Override
    public boolean deleteOne(long primaryKey, K secondaryKey) {
        PrimaryDelayCache<K, V> primaryCache = primaryCacheMap.computeIfAbsent(primaryKey, PrimaryDelayCache::new);
        primaryCache.deleteCacheValue(secondaryKey);
        return true;
    }

    @Override
    public boolean deleteBatch(long primaryKey, Collection<K> secondaryKeys) {
        PrimaryDelayCache<K, V> primaryCache = primaryCacheMap.computeIfAbsent(primaryKey, PrimaryDelayCache::new);
        for (K secondaryKey : secondaryKeys) {
            primaryCache.deleteCacheValue(secondaryKey);
        }
        return true;
    }

    @Override
    public V cloneValue(V value) {
        return cacheSource.cloneValue(value);
    }

    @Override
    public IClassConverter<K, V> getConverter() {
        return cacheSource.getConverter();
    }

    @Override
    public ICacheKeyValueBuilder<K> getKeyValueBuilder() {
        return cacheSource.getKeyValueBuilder();
    }

    @Override
    public ICacheDelaySource<K, V> createDelayUpdateSource(ICacheExecutor executor) {
        return this;
    }

    @Override
    public boolean flushAll(long currentTime) {
        String string = EvnCoreConfigs.getString("cache.flush.logPath");
        File parent = new File(string + "_" + currentTime);
        if (!parent.exists()){
            boolean success = parent.mkdir();
            if (!success){
                logger.error("mkdir error:{}", string);
            }
        }
        ICacheUniqueId cacheDaoUnique = getCacheUniqueId();
        ICacheKeyValueBuilder<K> keyValueBuilder = getKeyValueBuilder();
        Collection<PrimaryDelayCache<K, V>> primaryDelayCaches = primaryCacheMap.values();
        for (PrimaryDelayCache<K, V> delayCache : primaryDelayCaches) {
            StringBuilder builder = new StringBuilder();
            long primaryKey = delayCache.getPrimaryKey();
            builder.append("primaryKey:").append(primaryKey).append("\n");
            Collection<KeyDataValue<K, V>> dataValues = delayCache.getAll();
            for (KeyDataValue<K, V> dataValue : dataValues) {
                String secondaryKeyString = keyValueBuilder.toSecondaryKeyString(dataValue.getKey());
                builder.append("key:").append(secondaryKeyString).append("\t")
                        .append("command:").append(dataValue.getCacheCommand().name()).append("\t")
                        .append("data:").append(getConverter().convert2Cache(dataValue.getDataValue())).append("\n");
            }
            try {
                String filename = String.format("%s.cache", cacheDaoUnique.getRedisKeyString(primaryKey));
                FileWriter writer = new FileWriter(parent.getAbsolutePath() + "/" + filename);
                writer.append(builder.toString());
                writer.flush();
                writer.close();
            }
            catch (IOException e) {
                logger.error("{}", builder.toString(), e);
            }
        }
        int tryCount = Math.max(2, EvnCoreConfigs.getInt("cache.flush.tryAllCount"));
        while (tryCount-- > 0 && !primaryCacheMap.isEmpty()){
            lockAndFlushPrimaryCache(primaryCacheMap.keySet(), "flushAll");
        }
        for (Map.Entry<Long, PrimaryDelayCache<K, V>> entry : primaryCacheMap.entrySet()) {
            String keyString = String.valueOf(entry.getKey());
            logger.error("{} primaryKey:{} flushAll error.", getAClass().getName(), getCacheUniqueId().getName(), keyString);
        }
        return true;
    }

    @Override
    public void flushOne(long primaryKey, long currentTime, Consumer<Boolean> consumer) {
        CacheCallable<Boolean> callable = new CacheCallable<>(getScheduleName(), () -> {
            boolean isSuccess = false;
            int tryCount = EvnCoreConfigs.getInt("cache.flush.tryOneCount");
            for (int count = 0; count < tryCount; count++) {
                isSuccess = lockAndFlushPrimaryCache(Collections.singletonList(primaryKey), "flushOne.0");
                if (isSuccess) {
                    break;
                }
                logger.error("{} primaryKey:{} flush count:{} flushOne.0 failure.", getAClass().getName(), primaryKey, count);
                ThreadUtil.sleep(50);
            }
            return isSuccess;
        }, consumer);
        executor.submit(callable);
    }

    @Override
    public boolean updateCacheInformation(long primaryKey, CacheInformation cacheInformation) {
        return true;
    }

    @Override
    public boolean flushOne(long primaryKey) {
        CacheCallable<Boolean> callable = new CacheCallable<>(getScheduleName(), () -> lockAndFlushPrimaryCache(Collections.singletonList(primaryKey), "flushOne.1"), null);
        boolean isSuccess = false;
        int tryCount = EvnCoreConfigs.getInt("cache.flush.tryOneCount");
        long flushTimeOut = EvnCoreConfigs.getDuration("cache.flush.timeOut", TimeUnit.MILLISECONDS);
        Throwable throwable = null;
        for (int count = 0; count < tryCount; count++) {
            try {
                ICacheFuture<Boolean> future = executor.submit(callable);
                isSuccess = future.get(flushTimeOut, TimeUnit.MILLISECONDS);
                if (isSuccess) {
                    break;
                }
                logger.error("{} primaryKey:{} count:{} flushOne.1 failure.", getAClass().getName(), primaryKey, count);
            }
            catch (Throwable t) {
                throwable = t;
                logger.error("{} primaryKey:{} count:{} flushOne.1 error.", getAClass().getName(), primaryKey, count, t);
            }
        }
        if (!isSuccess && throwable != null){
            throw new CacheException("%s %s", primaryKey, getAClass().getName(), throwable);
        }
        return isSuccess;
    }

    protected String getScheduleName() {
        return getCacheUniqueId().getName();
    }

    @Override
    public ICacheSource<K, V> getCacheSource() {
        return cacheSource;
    }

    @Override
    public void addFlushCallback(Consumer<PrimaryDelayCache<K, V>> consumer) {
        flushCallbacks.add(consumer);
    }

    protected abstract Map<Long, PrimaryDelayCache<K, V>> executeWritePrimaryCache(Map<Long, PrimaryDelayCache<K, V>> primaryCacheMap);

    private void onScheduleAll() {
        if (primaryCacheMap.isEmpty()) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        int maximumCount = Math.min(50, EvnCoreConfigs.getInt("cache.flush.maximumCount"));
        List<Long> removePrimaryKeyList = new ArrayList<>();
        for (Map.Entry<Long, PrimaryDelayCache<K, V>> entry : primaryCacheMap.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            if (removePrimaryKeyList.size() >= maximumCount){
                break;
            }
            else {
                removePrimaryKeyList.add(entry.getKey());
            }
        }
        lockAndFlushPrimaryCache(removePrimaryKeyList, "onScheduleAll");
    }

    private boolean lockAndFlushPrimaryCache(Collection<Long> removePrimaryKeyList, String message){
        if (removePrimaryKeyList.isEmpty()) {
            return true;
        }
        List<LockKey> lockKeyList = removePrimaryKeyList.stream().map(this::getLockKey).collect(Collectors.toList());
        return LockUtil.syncLock(lockKeyList, getAClass().getName() + ":" + message, ()->{
            Map<Long, PrimaryDelayCache<K, V>> primaryCacheMap0 = new HashMap<>();
            for (long removePrimaryKey : removePrimaryKeyList) {
                PrimaryDelayCache<K, V> primaryCache = primaryCacheMap.remove(removePrimaryKey);
                if (primaryCache == null) {
                    continue;
                }
                if (primaryCache.isEmpty()) {
                    continue;
                }
                primaryCacheMap0.put(removePrimaryKey, primaryCache);
            }
            return flushPrimaryCacheInLock(primaryCacheMap0);
        });
    }

    private boolean flushPrimaryCacheInLock(Map<Long, PrimaryDelayCache<K, V>> primaryCacheMap) {
        Map<Long, PrimaryDelayCache<K, V>> failurePrimaryCacheMap = executeWritePrimaryCache(primaryCacheMap);
        long duration = RandomUtils.nextLong(1000, 5000);
        for (Map.Entry<Long, PrimaryDelayCache<K, V>> entry : failurePrimaryCacheMap.entrySet()) {
            long primaryKey = entry.getValue().getPrimaryKey();
            PrimaryDelayCache<K, V> newPrimaryCache = this.primaryCacheMap.computeIfAbsent(primaryKey, key -> new PrimaryDelayCache<>(key, duration));
            newPrimaryCache.rollbackAll(entry.getValue().getAll());
        }
        for (Map.Entry<Long, PrimaryDelayCache<K, V>> entry : primaryCacheMap.entrySet()) {
            PrimaryDelayCache<K, V> primaryCache = entry.getValue();
            PrimaryDelayCache<K, V> failureCache = failurePrimaryCacheMap.get(entry.getKey());
            if (failureCache != null && !failureCache.isEmpty()){
                for (KeyDataValue<K, V> dataValue : failureCache.getAll()) {
                    primaryCache.remove(dataValue.getKey());
                }
            }
            if (primaryCache.isEmpty()){
                continue;
            }
            for (Consumer<PrimaryDelayCache<K, V>> flushCallback : flushCallbacks) {
                try {
                    flushCallback.accept(primaryCache);
                }
                catch (Throwable t){
                    logger.error("{} primaryKey:{} callback error.", getAClass().getName(), entry.getKey(), t);
                }
            }
        }

        return failurePrimaryCacheMap.isEmpty();
    }

    private List<V> replaceAllDataValueList(long primaryKey, List<V> dateValueList) {
        PrimaryDelayCache<K, V> primaryCache = primaryCacheMap.get(primaryKey);
        if (primaryCache == null) {
            return dateValueList;
        }
        Collection<KeyDataValue<K, V>> dataValues = primaryCache.getAll();
        if (dataValues.isEmpty()) {
            return dateValueList;
        }
        Map<K, V> key2DataValueMap = dateValueList.stream().collect(Collectors.toMap(V::secondaryKey, dataValue -> dataValue));
        for (KeyDataValue<K, V> dataValue : dataValues) {
            if (dataValue.isDeleted()) {
                key2DataValueMap.remove(dataValue.getKey());
            } else {
                key2DataValueMap.put(dataValue.getKey(), dataValue.getDataValue());
            }
        }
        return new ArrayList<>(key2DataValueMap.values());
    }

    /**
     * @param name
     * @param modelList
     * @param cacheList
     * @param function
     * @param <T>
     * @param <V>
     * @return 返回失败的数据
     */
    public static <T, V> List<V> handleBatch(String name, List<T> modelList, List<V> cacheList, Function<List<T>, Boolean> function){
        if (modelList.size() != cacheList.size()){
            throw new UnsupportedOperationException("");
        }
        if (modelList.isEmpty()){
            return Collections.emptyList();
        }
        List<V> failureCacheList = new ArrayList<>();
        int batchCount = EvnCoreConfigs.getInt("cache.flush.batchCount");
        while (modelList.size() > 0) {
            int count = modelList.size() / batchCount;
            int index = count * batchCount;
            if (modelList.size() % batchCount == 0){
                index -= batchCount;                 //刚好是整数的时候
            }
            boolean success = false;
            try {
                success = function.apply(modelList.subList(index, modelList.size()));
            }
            catch (Throwable t){
                logger.error("name:{} index:{} modelList:{}", name, index,  modelList.size(), t);
            }
            finally {
                if (success){
                }
                else {
                    failureCacheList.addAll(cacheList.subList(index, modelList.size()));
                }
            }
            modelList = index > 0 ? modelList.subList(0, index) : Collections.emptyList();
        }
        return failureCacheList;
    }
}