package com.game.core.cache.source;

import com.game.common.config.EvnCoreConfigs;
import com.game.common.config.EvnCoreType;
import com.game.common.lock.LockKey;
import com.game.common.util.CommonUtil;
import com.game.common.util.RandomUtil;
import com.game.core.cache.CacheType;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.data.DataCollection;
import com.game.core.cache.data.DataSourceUtil;
import com.game.core.cache.data.IData;
import com.game.core.cache.mapper.IClassConverter;
import com.game.core.cache.source.executor.CacheRunnable;
import com.game.core.cache.source.executor.CacheSourceUtil;
import com.game.core.cache.source.executor.ICacheExecutor;
import com.game.core.cache.source.executor.ICacheSource;
import com.mongodb.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class CacheDelaySource<K, V extends IData<K>> implements ICacheDelaySource<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(CacheDelaySource.class);

    private final CacheDbSource<K, V> cacheSource;
    private final Map<Long, PrimaryDelayCache<K, V>> primaryCacheMap;
    private Consumer<PrimaryDelayCache<K, V>> writeBackSuccessCallBack;
    private final AtomicLong versionIdGen;

    public CacheDelaySource(CacheDbSource<K, V> cacheSource) {
        this.cacheSource = cacheSource;
        this.primaryCacheMap = new ConcurrentHashMap<>();
        this.writeBackSuccessCallBack = null;
        this.versionIdGen = new AtomicLong(0);
        //初始化~
        CacheRunnable cacheRunnable = new CacheRunnable(getJobName(), this::onScheduleAll);
        long randomValue = RandomUtil.nextLong(1000, 2000);
        long initialDelay = randomValue * 50 / 50;    //取100ms的整数倍
        cacheSource.getExecutor().scheduleWithFixedDelay(cacheRunnable, initialDelay, 500L, TimeUnit.MILLISECONDS);
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
        }
        else {
            return cacheSource.get(primaryKey, secondaryKey);
        }
    }

    @Override
    public List<V> getAll(long primaryKey) {
        List<V> dataList = cacheSource.getAll(primaryKey);
        return replaceWithDelayDataCommand(primaryKey, dataList);
    }

    @Override
    public DataCollection<K, V> getCollection(long primaryKey) {
        DataCollection<K, V> collection = cacheSource.getCollection(primaryKey);
        List<V> dataList = replaceWithDelayDataCommand(primaryKey, collection.getDataList());
        collection.updateDataList(dataList);
        return collection;
    }

    @Override
    public boolean replaceOne(long primaryKey, V data) {
        V cloneValue = cloneValue(data);
        PrimaryDelayCache<K, V> primaryCache = primaryCacheMap.computeIfAbsent(primaryKey, this::cretePrimaryDelayCache);
        primaryCache.add(KeyDataCommand.upsertCommand(cloneValue, versionIdGen.incrementAndGet()));
        return true;
    }

    @Override
    public boolean replaceBatch(long primaryKey, Collection<V> dataList) {
        if (dataList.isEmpty()){
            return true;
        }
        PrimaryDelayCache<K, V> primaryCache = primaryCacheMap.computeIfAbsent(primaryKey, this::cretePrimaryDelayCache);
        long versionId = versionIdGen.incrementAndGet();
        for (V value : dataList) {
            V cloneValue = cloneValue(value);
            primaryCache.add(KeyDataCommand.upsertCommand(cloneValue, versionId));
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
        PrimaryDelayCache<K, V> primaryCache = primaryCacheMap.computeIfAbsent(primaryKey, this::cretePrimaryDelayCache);
        primaryCache.add(KeyDataCommand.deleteCommand(secondaryKey, versionIdGen.incrementAndGet()));
        return true;
    }

    @Override
    public boolean deleteBatch(long primaryKey, Collection<K> secondaryKeys) {
        if (secondaryKeys.isEmpty()) {
            return true;
        }
        PrimaryDelayCache<K, V> primaryCache = primaryCacheMap.computeIfAbsent(primaryKey, this::cretePrimaryDelayCache);
        long versionId = versionIdGen.incrementAndGet();
        for (K secondaryKey : secondaryKeys) {
            primaryCache.add(KeyDataCommand.deleteCommand(secondaryKey, versionId));
        }
        return true;
    }

    @Override
    public V cloneValue(V data) {
        return cacheSource.cloneValue(data);
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
    public ICacheDelaySource<K, V> createDelayUpdateSource() {
        return this;
    }

    @Override
    public boolean flushAll(long currentTime) {
        String string = EvnCoreConfigs.getInstance(EvnCoreType.CACHE).getString("flush.logPath");
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
            for (KeyDataCommand<K, V> dataCommand : delayCache.getAllDataCommands()) {
                String secondaryKeyString = keyValueBuilder.toSecondaryKeyString(dataCommand.getKey());
                builder.append("key:").append(secondaryKeyString).append("\t")
                        .append("data:").append(getConverter().convert2Cache(dataCommand.getData())).append("\n");
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
        CacheSourceUtil.submitCallable(this,"flushAll", () -> {
            int tryCount = Math.max(2, EvnCoreConfigs.getInstance(EvnCoreType.CACHE).getInt("flush.tryAllCount"));
            while (tryCount-- > 0 && !primaryCacheMap.isEmpty()){
                flushPrimaryCacheDirectly(primaryCacheMap.keySet());
            }
            for (Map.Entry<Long, PrimaryDelayCache<K, V>> entry : primaryCacheMap.entrySet()) {
                String keyString = String.valueOf(entry.getKey());
                logger.error("{} primaryKey:{} flushAll error.", getAClass().getName(), getCacheUniqueId().getName(), keyString);
            }
            return primaryCacheMap.isEmpty();
        }, null);
        return true;
    }

    @Override
    public void flushOne(long primaryKey, long currentTime, Consumer<Boolean> consumer) {
        CacheSourceUtil.submitCallable(this, "flushOne", () -> {
            boolean isSuccess = flushPrimaryCacheDirectly(Collections.singletonList(primaryKey));
            if (isSuccess) {
                primaryCacheMap.remove(primaryKey);
            }
            else {
                logger.error("{} primaryKey:{} flushOne failure.", getAClass().getName(), primaryKey);
            }
            return isSuccess;
        }, consumer);
    }

    protected String getJobName() {
        return getCacheUniqueId().getName();
    }

    @Override
    public ICacheSource<K, V> getCacheSource() {
        return cacheSource;
    }

    @Override
    public CacheType getCacheType() {
        return cacheSource.getCacheType();
    }

    @Override
    public ICacheExecutor getExecutor() {
        return cacheSource.getExecutor();
    }

    @Override
    public void setWriteBackCallback(Consumer<PrimaryDelayCache<K, V>> consumer) {
        writeBackSuccessCallBack = consumer;
    }

    protected abstract Map<Long, PrimaryDelayCache<K, V>> executeWritePrimaryCache(Map<Long, PrimaryDelayCache<K, V>> primaryCacheMap);

    private void onScheduleAll() {
        if (primaryCacheMap.isEmpty()) {
            return;
        }
        List<PrimaryDelayCache<K, V>> primaryDelayCacheList = primaryCacheMap.values().stream()
                .sorted(Comparator.comparingLong(PrimaryDelayCache::getExpiredTime))
                .collect(Collectors.toList());
        long currentTime = System.currentTimeMillis();
        int maximumCount = Math.min(50, EvnCoreConfigs.getInstance(EvnCoreType.CACHE).getInt("flush.maximumCount"));
        List<Long> writeBackPrimaryKeyList = new ArrayList<>();
        for (PrimaryDelayCache<K, V> primaryDelayCache : primaryDelayCacheList) {
            if (writeBackPrimaryKeyList.size() >= maximumCount){
                break;
            }
            else if (primaryDelayCache.isExpired(currentTime)){
                writeBackPrimaryKeyList.add(primaryDelayCache.getPrimaryKey());
            }
        }
        CacheSourceUtil.submitCallable(this, "onScheduleAll", () -> {
            boolean isSuccess = flushPrimaryCacheDirectly(writeBackPrimaryKeyList);
            if (!isSuccess) {
                logger.error("{} onScheduleAll failure.", getAClass().getName());
            }
            return isSuccess;
        }, null);
    }

    private boolean flushPrimaryCacheDirectly(Collection<Long> removePrimaryKeyList){
        if (removePrimaryKeyList.isEmpty()) {
            return true;
        }
        Map<Long, PrimaryDelayCache<K, V>> primaryCacheMap0 = new HashMap<>();
        for (long removePrimaryKey : removePrimaryKeyList) {
            PrimaryDelayCache<K, V> primaryCache = primaryCacheMap.get(removePrimaryKey);
            if (primaryCache == null || primaryCache.isEmpty()){
                continue;
            }
            primaryCacheMap0.put(removePrimaryKey, primaryCache.shallowCopy());
        }
        return flushPrimaryCacheDirectly(primaryCacheMap0);
    }


    private boolean flushPrimaryCacheDirectly(Map<Long, PrimaryDelayCache<K, V>> primaryCacheMap) {
        Map<Long, PrimaryDelayCache<K, V>> successPrimaryCacheMap = executeWritePrimaryCache(primaryCacheMap);
        for (Map.Entry<Long, PrimaryDelayCache<K, V>> entry : successPrimaryCacheMap.entrySet()) {
            long primaryKey = entry.getValue().getPrimaryKey();
            PrimaryDelayCache<K, V> currentPrimaryCache = primaryCacheMap.get(primaryKey);
            if (currentPrimaryCache == null){
                continue;
            }
            // 锁住不让更新操作, 所以不会改动primaryCacheMap的数据
            DataSourceUtil.syncLock(cacheSource,primaryKey, "flushPrimaryCacheDirectly", ()->{
                PrimaryDelayCache<K, V> successPrimaryCache = entry.getValue();
                ArrayList<KeyDataCommand<K, V>> dataCommands = new ArrayList<>(successPrimaryCache.getAllDataCommands());
                for (KeyDataCommand<K, V> dataCommand : dataCommands) {
                    KeyDataCommand<K, V> kvKeyDataCommand = currentPrimaryCache.get(dataCommand.getKey());
                    if (kvKeyDataCommand == null || kvKeyDataCommand.getVersionId() <= dataCommand.getVersionId()) {
                        currentPrimaryCache.remove(dataCommand.getKey());
                    }
                    else {
                        successPrimaryCache.remove(dataCommand.getKey());
                    }
                }
                if (currentPrimaryCache.isEmpty()) {
                    primaryCacheMap.remove(primaryKey);
                }
                writeBackSuccessCallBack.accept(successPrimaryCache);
                return true;
            });
        }
        return successPrimaryCacheMap.isEmpty();
    }

    private List<V> replaceWithDelayDataCommand(long primaryKey, List<V> dataList) {
        PrimaryDelayCache<K, V> primaryCache = primaryCacheMap.get(primaryKey);
        if (primaryCache == null) {
            return dataList;
        }
        Map<K, V> stream2Map = CommonUtil.stream2Map(dataList, V::secondaryKey);
        for (KeyDataCommand<K, V> dataCommand : primaryCache.getAllDataCommands()) {
            if (dataCommand.isDeleted()) {
                stream2Map.remove(dataCommand.getKey());
            }
            else if (dataCommand.isUpsert()){
                stream2Map.put(dataCommand.getKey(), dataCommand.getData());
            }
        }
        return new ArrayList<>(stream2Map.values());
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
        List<V> successCacheList = new ArrayList<>();
        int batchCount = EvnCoreConfigs.getInstance(EvnCoreType.CACHE).getInt("flush.batchCount");
        while (modelList.size() > 0) {
            int count = modelList.size() / batchCount;
            int index = count * batchCount;
            if (modelList.size() % batchCount == 0){
                index -= batchCount;                 //刚好是整数的时候
            }
            try {
                boolean success = function.apply(modelList.subList(index, modelList.size()));
                if (success){
                    successCacheList.addAll(cacheList.subList(index, modelList.size()));
                }
            }
            catch (Throwable t){
                logger.error("name:{} index:{} modelList:{}", name, index,  modelList.size(), t);
            }
            modelList = index > 0 ? modelList.subList(0, index) : Collections.emptyList();
        }
        return successCacheList;
    }

    protected PrimaryDelayCache cretePrimaryDelayCache(long primaryKey){
        long duration = EvnCoreConfigs.getInstance(EvnCoreType.CACHE).getDuration("flush.expiredDuration", TimeUnit.MILLISECONDS);
        return new PrimaryDelayCache(primaryKey, System.currentTimeMillis() + duration);
    }
}