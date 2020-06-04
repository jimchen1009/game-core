package com.game.cache.source;

import com.game.cache.data.IData;
import com.game.cache.exception.CacheException;
import com.game.cache.mapper.ClassConfig;
import com.game.cache.source.executor.CacheCallable;
import com.game.cache.source.executor.CacheRunnable;
import com.game.cache.source.executor.ICacheExecutor;
import com.game.cache.source.executor.ICacheSource;
import com.game.common.config.Configs;
import com.game.common.lock.LockKey;
import com.game.common.lock.LockUtil;
import com.game.common.log.LogUtil;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class CacheDelayUpdateSource<PK, K, V extends IData<K>> implements ICacheDelayUpdateSource<PK, K, V> {

    private static final Logger logger = LoggerFactory.getLogger(CacheDelayUpdateSource.class);

    public static final Map<String, Object> EMPTY = Collections.emptyMap();

    private final CacheSource<PK, K, V> cacheSource;
    private final Map<PK, PrimaryCache<K>> primaryCacheMap;
    private final ICacheExecutor executor;

    public CacheDelayUpdateSource(CacheSource<PK, K, V> cacheSource, ICacheExecutor executor) {
        this.cacheSource = cacheSource;
        this.primaryCacheMap = new ConcurrentHashMap<>();
        this.executor = executor;
        //初始化~
        CacheRunnable cacheRunnable = new CacheRunnable(getScheduleName(), this::onSchedule);
        long randomValue = RandomUtils.nextLong(1000, 2000);
        long initialDelay = randomValue * 50 / 50;    //取100ms的整数倍
        executor.scheduleAtFixedRate(cacheRunnable, initialDelay, 2000L, TimeUnit.MILLISECONDS);
    }

    @Override
    public LockKey getLockKey(PK primaryKey) {
        return cacheSource.getLockKey(primaryKey);
    }

    @Override
    public LockKey getSharedLockKey(PK primaryKey) {
        return getSharedLockKey(primaryKey);
    }

    @Override
    public Class<V> getAClass() {
        return cacheSource.getAClass();
    }

    @Override
    public ClassConfig getClassConfig() {
        return cacheSource.getClassConfig();
    }

    @Override
    public Map<String, Object> get(PK primaryKey, K secondaryKey) {
        PrimaryCache<K> primaryCache = primaryCacheMap.get(primaryKey);
        return primaryCache == null ? cacheSource.get(primaryKey, secondaryKey) : primaryCache.getCacheValue(secondaryKey);
    }

    @Override
    public Collection<Map<String, Object>> getAll(PK primaryKey) {
        Collection<Map<String, Object>> cacheValues = cacheSource.getAll(primaryKey);
        return replaceAllCacheValues(primaryKey, cacheValues);
    }

    @Override
    public CacheCollection getCollection(PK primaryKey) {
        CacheCollection cacheCollection = cacheSource.getCollection(primaryKey);
        Collection<Map<String, Object>> allCacheValues = replaceAllCacheValues(primaryKey, cacheCollection.getCacheValuesList());
        cacheCollection.setCacheValueList(allCacheValues);
        return cacheCollection;
    }

    @Override
    public boolean replaceOne(PK primaryKey, KeyCacheValue<K> keyCacheValue) {
        PrimaryCache<K> kPrimaryCache = primaryCacheMap.computeIfAbsent(primaryKey, key -> new PrimaryCache<>());
        kPrimaryCache.add(keyCacheValue);
        return true;
    }

    @Override
    public boolean replaceBatch(PK primaryKey, List<KeyCacheValue<K>> keyCacheValues) {
        PrimaryCache<K> kPrimaryCache = primaryCacheMap.computeIfAbsent(primaryKey, key -> new PrimaryCache<>());
        kPrimaryCache.addAll(keyCacheValues);
        return true;
    }

    @Override
    public boolean deleteOne(PK primaryKey, K secondaryKey) {
        PrimaryCache<K> kPrimaryCache = primaryCacheMap.computeIfAbsent(primaryKey, key -> new PrimaryCache<>());
        kPrimaryCache.add(new KeyCacheValue<>(secondaryKey, CacheCommand.DELETE, EMPTY));
        return true;
    }

    @Override
    public boolean deleteBatch(PK primaryKey, Collection<K> secondaryKeys) {
        PrimaryCache<K> kPrimaryCache = primaryCacheMap.computeIfAbsent(primaryKey, key -> new PrimaryCache<>());
        List<KeyCacheValue<K>> keyCacheValueList = secondaryKeys.stream().map(secondaryKey -> new KeyCacheValue<>(secondaryKey, CacheCommand.DELETE, EMPTY)).collect(Collectors.toList());
        kPrimaryCache.addAll(keyCacheValueList);
        return true;
    }

    @Override
    public ICacheDelayUpdateSource<PK, K, V> createDelayUpdateSource(ICacheExecutor executor) {
        return this;
    }

    @Override
    public void executePrimaryCacheFlushAsync(PK primaryKey, Consumer<Boolean> consumer){
        CacheCallable<Boolean> callable = createPrimaryCacheFlushCallable(primaryKey, "executePrimaryCacheFlushAsync", consumer);
        executor.submit(callable);
    }

    @Override
    public boolean executePrimaryCacheFlushSync(PK primaryKey) {
        CacheCallable<Boolean> callable = createPrimaryCacheFlushCallable(primaryKey, "executePrimaryCacheFlushSync", null);
        boolean isSuccess = false;
        int flushTryCount = Configs.getInstance().getInt("cache.flush.tryCount");
        long flushTimeOut = Configs.getInstance().getDuration("cache.flush.timeOut", TimeUnit.MILLISECONDS);
        for (int count = 0; count < flushTryCount; count++) {
            try {
                Future<Boolean> future = executor.submit(callable);
                isSuccess = future.get(flushTimeOut, TimeUnit.MILLISECONDS);
                if (isSuccess){
                    break;
                }
                logger.error("primaryKey:{}.{} count:{} flush failure.", LogUtil.toJSONString(primaryKey), getAClass().getName(), count);
            }
            catch (Throwable t) {
                throw new CacheException(LogUtil.toJSONString(primaryKey), t);
            }
        }
        return isSuccess;
    }

    private CacheCallable<Boolean> createPrimaryCacheFlushCallable(PK primaryKey, String message,  Consumer<Boolean> consumer){
        CacheCallable<Boolean> callable = new CacheCallable<>(getScheduleName(), () -> {
            Boolean isSuccess = LockUtil.syncLock(getLockKey(primaryKey), message, () -> {
                PrimaryCache<K> primaryCache = primaryCacheMap.remove(primaryKey);
                if (primaryCache == null) {
                    return true;
                }
                Collection<KeyCacheValue<K>> keyCacheValues = primaryCache.getAll();
                return flushKeyCacheValuesInLock(Collections.singletonMap(primaryKey, keyCacheValues));
            });
            return isSuccess;
        }, consumer);
        return callable;
    }

    protected String getScheduleName() {
        return cacheSource.getClassConfig().tableName;
    }

    @Override
    public ICacheSource<PK, K, V> getCacheSource() {
        return cacheSource;
    }

    protected abstract Map<PK, List<KeyCacheValue<K>>> executeWriteBackKeyCacheValues(Map<PK, Collection<KeyCacheValue<K>>> keyCacheValuesMap);

    private void onSchedule() {
        if (primaryCacheMap.isEmpty()){
            return;
        }
        long currentTime = System.currentTimeMillis();
        Map<PK, Collection<KeyCacheValue<K>>> keyCacheValuesMap = new HashMap<>();
        List<PK> removePrimaryKeyList = new ArrayList<>();
        List<Map.Entry<PK, Long>> expiredPrimaryKey = new ArrayList<>();
        for (Map.Entry<PK, PrimaryCache<K>> entry : primaryCacheMap.entrySet()) {
            if (entry.getValue().isEmpty()){
                continue;
            }
            if (currentTime < entry.getValue().getExpiredTime()) {
                expiredPrimaryKey.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().getExpiredTime()));
            }
            else {
                removePrimaryKeyList.add(entry.getKey());
            }
        }
        int maximumCount = Configs.getInstance().getInt("cache.maximumCount");
        if (expiredPrimaryKey.size() > maximumCount){
            expiredPrimaryKey.sort(Comparator.comparingLong(Map.Entry::getValue));
            for (int i = maximumCount; i < expiredPrimaryKey.size(); i++) {
                removePrimaryKeyList.add(expiredPrimaryKey.get(i).getKey());
            }
        }
        if (removePrimaryKeyList.isEmpty()){
            return;
        }
        for (PK pk : removePrimaryKeyList) {
            PrimaryCache<K> primaryCache = primaryCacheMap.remove(pk);
            if (primaryCache == null){
                continue;
            }
            Collection<KeyCacheValue<K>> keyCacheValues = primaryCache.getAll();
            if (keyCacheValues.isEmpty()){
                continue;
            }
            keyCacheValuesMap.put(pk, keyCacheValues);
        }
        flushKeyCacheValuesInLock(keyCacheValuesMap);
    }

    private Collection<Map<String, Object>> replaceAllCacheValues(PK primaryKey, Collection<Map<String, Object>> cacheValues){
        PrimaryCache<K> primaryCache = primaryCacheMap.get(primaryKey);
        if (primaryCache  == null){
            return cacheValues;
        }
        ICacheKeyValueBuilder<PK, K> keyValueBuilder = cacheSource.getKeyValueBuilder();
        List<Map<String, Object>> replaceCacheValues = new ArrayList<>(cacheValues.size());
        for (Map<String, Object> cacheValue : cacheValues) {
            K secondaryKey = keyValueBuilder.createSecondaryKey(cacheValue);
            KeyCacheValue<K> keyCacheValue = primaryCache.get(secondaryKey);
            if (keyCacheValue == null){
                replaceCacheValues.add(cacheValue);
            }
            else if (keyCacheValue.isDeleted()){
                //被删掉了~
            }
            else {
                HashMap<String, Object> createCacheValue = new HashMap<>(cacheValue);
                createCacheValue.putAll(keyCacheValue.getCacheValue());
                replaceCacheValues.add(createCacheValue);
            }
        }
        return replaceCacheValues;
    }


    private boolean flushKeyCacheValuesInLock(Map<PK, Collection<KeyCacheValue<K>>> keyCacheValuesMap){
        if (keyCacheValuesMap.isEmpty()) {
            return true;
        }
        Map<PK, List<KeyCacheValue<K>>> failureKeyCacheValuesMap = executeWriteBackKeyCacheValues(keyCacheValuesMap);
        for (Map.Entry<PK, List<KeyCacheValue<K>>> entry : failureKeyCacheValuesMap.entrySet()) {
            int duration = RandomUtils.nextInt(0, 5);
            PrimaryCache<K> kPrimaryCache = primaryCacheMap.computeIfAbsent(entry.getKey(), key -> new PrimaryCache<>(duration));
            kPrimaryCache.rollbackAll(entry.getValue());
        }
        return failureKeyCacheValuesMap.isEmpty();
    }

    private static final class PrimaryCache<K>{

        public static final Map<String, Object> EMPTY = new HashMap<>(0);

        private volatile long expiredTime;
        private final Map<K, KeyCacheValue<K>> keyCacheValuesMap;

        public PrimaryCache(int duration) {
            if (duration == 0){
                duration = Configs.getInstance().getInt("cache.flush.expiredDuration");
            }
            this.expiredTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(duration);
            this.keyCacheValuesMap = new ConcurrentHashMap<>();
        }

        public PrimaryCache() {
            this(0);
        }

        public long getExpiredTime() {
            return expiredTime;
        }

        public void setExpiredTime(long expiredTime) {
            this.expiredTime = expiredTime;
        }

        public boolean isEmpty(){
            return keyCacheValuesMap.isEmpty();
        }

        /**
         * @param secondaryKey
         * @return
         */
        public Map<String, Object> getCacheValue(K secondaryKey){
            KeyCacheValue<K> keyCacheValue = keyCacheValuesMap.get(secondaryKey);
            if (keyCacheValue == null || keyCacheValue.isDeleted()){
                return null;
            }
            return keyCacheValue.getCacheValue();
        }

        /**
         * @return
         */
        public Collection<Map<String, Object>> getAllCacheValues(){
            List<Map<String, Object>> cacheValueList = new ArrayList<>();
            for (Map.Entry<K, KeyCacheValue<K>> entry : keyCacheValuesMap.entrySet()) {
                if (entry.getValue().isDeleted()){
                  continue;
                }
                cacheValueList.add(entry.getValue().getCacheValue());
            }
            return cacheValueList;
        }

        /**
         * @param secondaryKey
         * @return
         */
        public Map<String, Object> deleteCacheValue(K secondaryKey){
            KeyCacheValue<K> oldKeyCacheValue = keyCacheValuesMap.remove(secondaryKey);
            if (oldKeyCacheValue == null){
                keyCacheValuesMap.put(secondaryKey, new KeyCacheValue<>(secondaryKey, CacheCommand.UPDATE, EMPTY));
                return null;
            }
            if (oldKeyCacheValue.isInsert()) {
                return oldKeyCacheValue.getCacheValue();
            }
            if (oldKeyCacheValue.isDeleted()) {
                return null;
            }
            if (oldKeyCacheValue.isUpdate()){
                keyCacheValuesMap.put(secondaryKey, new KeyCacheValue<>(secondaryKey, CacheCommand.UPDATE, EMPTY));
                return oldKeyCacheValue.getCacheValue();
            }
            throw new CacheException(oldKeyCacheValue.getCacheCommand().name());
        }

        /**
         * @param keyCacheValue
         */
        public void add(KeyCacheValue<K> keyCacheValue){
            add0(keyCacheValue);
        }

        public KeyCacheValue<K> get(K secondaryKey){
            return keyCacheValuesMap.get(secondaryKey);
        }

        public void addAll(Collection<KeyCacheValue<K>> keyCacheValues){
            for (KeyCacheValue<K> keyCacheValue : keyCacheValues) {
                add0(keyCacheValue);
            }
        }

        private void add0(KeyCacheValue<K> keyCacheValue){
            K secondaryKey = keyCacheValue.getKey();
            KeyCacheValue<K> oldPrimaryCacheValue = keyCacheValuesMap.get(secondaryKey);
            if (oldPrimaryCacheValue == null || oldPrimaryCacheValue.isDeleted()) {
                keyCacheValuesMap.put(secondaryKey, keyCacheValue);
            }
            else if (oldPrimaryCacheValue.isInsert() && keyCacheValue.isDeleted()){
                keyCacheValuesMap.remove(secondaryKey);
            }
            else {
                HashMap<String, Object> currentCacheValues = new HashMap<>();
                currentCacheValues.putAll(oldPrimaryCacheValue.getCacheValue());
                currentCacheValues.putAll(keyCacheValue.getCacheValue());
                KeyCacheValue<K> newKeyCacheValue = new KeyCacheValue<>(secondaryKey, oldPrimaryCacheValue.getCacheCommand(), currentCacheValues);
                keyCacheValuesMap.put(secondaryKey, newKeyCacheValue);
            }
        }

        /**
         * @param primaryCacheValues
         */
        public void rollbackAll(Collection<KeyCacheValue<K>> primaryCacheValues){
            for (KeyCacheValue<K> primaryCacheValue : primaryCacheValues) {
                K secondaryKey = primaryCacheValue.getKey();
                keyCacheValuesMap.merge(secondaryKey, primaryCacheValue, (oldPrimaryCacheValue, addPrimaryCacheValue)->{
                    if (oldPrimaryCacheValue.isDeleted() || oldPrimaryCacheValue.isInsert()) {
                        return oldPrimaryCacheValue;
                    }
                    HashMap<String, Object> currentCacheValues = new HashMap<>();
                    currentCacheValues.putAll(addPrimaryCacheValue.getCacheValue());
                    currentCacheValues.putAll(oldPrimaryCacheValue.getCacheValue());
                    return new KeyCacheValue<>(secondaryKey, CacheCommand.UPDATE, currentCacheValues);
                });
            }
        }

        public Collection<KeyCacheValue<K>> getAll(){
            return keyCacheValuesMap.values();
        }
    }
}
