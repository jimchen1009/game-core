package com.game.core.cache.source.compose;

import com.game.common.lock.LockKey;
import com.game.core.cache.CacheInformation;
import com.game.core.cache.CacheType;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.data.DataBitIndex;
import com.game.core.cache.data.DataCollection;
import com.game.core.cache.data.DataPrivilegeUtil;
import com.game.core.cache.data.IData;
import com.game.core.cache.mapper.IClassConverter;
import com.game.core.cache.source.ICacheDelaySource;
import com.game.core.cache.source.ICacheKeyValueBuilder;
import com.game.core.cache.source.KeyDataValue;
import com.game.core.cache.source.PrimaryDelayCache;
import com.game.core.cache.source.executor.CacheRunnable;
import com.game.core.cache.source.executor.ICacheExecutor;
import com.game.core.cache.source.executor.ICacheSource;
import com.game.core.cache.source.redis.ICacheRedisSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 删除的操作还没有实现~
 * @param <K>
 * @param <V>
 */
public class CacheComposeSource<K, V extends IData<K>> implements ICacheComposeSource<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(CacheComposeSource.class);

    private final ICacheRedisSource<K, V> redisSource;
    private final ICacheSource<K, V> dbSource;

    private final Map<Long, CacheInformation> cacheInformationMap;

    public CacheComposeSource(ICacheRedisSource<K, V> redisSource, ICacheSource<K, V> dbSource, ICacheExecutor executor) {
        this.redisSource = redisSource;
        this.dbSource = dbSource;
        if (dbSource instanceof ICacheDelaySource){
            ((ICacheDelaySource<K, V>)dbSource).addFlushCallback(this::onPrimaryDBCacheSuccess);
        }
        this.cacheInformationMap = new ConcurrentHashMap<>();
        String name = "cacheCompose." + dbSource.getCacheUniqueId().getName();
        executor.scheduleAtFixedRate(new CacheRunnable(name, this::onScheduleAll), 60, 60, TimeUnit.SECONDS);
    }

    @Override
    public CacheType getCacheType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LockKey getLockKey(long primaryKey) {
        return redisSource.getLockKey(primaryKey);
    }

    @Override
    public V get(long primaryKey, K secondaryKey) {
        V data = redisSource.get(primaryKey, secondaryKey);
        if (data == null){
            data = dbSource.get(primaryKey, secondaryKey);
        }
        return data;
    }

    @Override
    public List<V> getAll(long primaryKey) {
        List<V> dataList = redisSource.getAll(primaryKey);
        if (dataList == null){
            dataList = dbSource.getAll(primaryKey);
        }
        return dataList;
    }

    @Override
    public DataCollection<K, V> getCollection(long primaryKey) {
        CacheInformation information = cacheInformationMap.get(primaryKey);
        long currentTime = System.currentTimeMillis();
        DataCollection<K, V> dataCollection;
        if (information != null && information.isExpired(currentTime)){
            dataCollection = getDBCollection(primaryKey, currentTime);
        }
        else {
            //第一次加载数据
            dataCollection = redisSource.getCollection(primaryKey);
            if (dataCollection == null || dataCollection.isExpired(currentTime)){
                dataCollection = getDBCollection(primaryKey, currentTime);
            }
            else {
                List<V> changeDataList = dataCollection.getDataList().stream().filter(data -> data.hasBitIndex(DataBitIndex.RedisChangeIndex)).collect(Collectors.toList());
                onReplaceRedisBatchSuccess(primaryKey, changeDataList);
            }
        }
        cacheInformationMap.put(primaryKey, dataCollection.getCacheInformation());
        return dataCollection;
    }

    @Override
    public boolean replaceOne(long primaryKey, V value) {
        beforeReplaceBatch(Collections.singleton(value));
        boolean isSuccess = redisSource.replaceOne(primaryKey, value);
        if (isSuccess){
            isSuccess = onReplaceRedisBatchSuccess(primaryKey, Collections.singleton(value));
        }
        return isSuccess;
    }

    @Override
    public boolean replaceBatch(long primaryKey, Collection<V> values) {
        beforeReplaceBatch(values);
        boolean isSuccess = redisSource.replaceBatch(primaryKey, values);
        if (isSuccess){
            isSuccess = onReplaceRedisBatchSuccess(primaryKey, values);
        }
        return isSuccess;
    }

    @Override
    public Class<V> getAClass() {
        return redisSource.getAClass();
    }


    @Override
    public ICacheUniqueId getCacheUniqueId() {
        return redisSource.getCacheUniqueId();
    }

    @Override
    public boolean deleteOne(long primaryKey, K secondaryKey) {
        boolean isSuccess = redisSource.deleteOne(primaryKey, secondaryKey);
        if (isSuccess){
            isSuccess = dbSource.deleteOne(primaryKey, secondaryKey);
        }
        return isSuccess;
    }

    @Override
    public boolean deleteBatch(long primaryKey, Collection<K> secondaryKeys) {
        boolean isSuccess = redisSource.deleteBatch(primaryKey, secondaryKeys);
        if (isSuccess){
            isSuccess = dbSource.deleteBatch(primaryKey, secondaryKeys);
        }
        return isSuccess;
    }

    @Override
    public V cloneValue(V value) {
        return dbSource.cloneValue(value);
    }

    @Override
    public IClassConverter<K, V> getConverter() {
        return dbSource.getConverter();
    }

    @Override
    public boolean flushAll(long currentTime) {
        return dbSource.flushAll(currentTime);
    }

    @Override
    public void flushOne(long primaryKey, long currentTime, Consumer<Boolean> consumer) {
        cacheInformationMap.remove(primaryKey);
        dbSource.flushOne(primaryKey, currentTime, consumer);
    }

    @Override
    public boolean updateCacheInformation(long primaryKey, CacheInformation cacheInformation) {
        return redisSource.updateCacheInformation(primaryKey, cacheInformation);
    }

    @Override
    public ICacheKeyValueBuilder<K> getKeyValueBuilder() {
        return redisSource.getKeyValueBuilder();
    }

    @Override
    public ICacheDelaySource<K, V> createDelayUpdateSource(ICacheExecutor executor) {
       throw new UnsupportedOperationException();
    }

    /**
     * 为了防止内存泄漏而已~
     */
    private void onScheduleAll(){
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<Long, CacheInformation>> iterator = cacheInformationMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<Long, CacheInformation> entry = iterator.next();
            if (currentTime < entry.getValue().getExpiredTime()){
                continue;
            }
            logger.error("primaryKey:{} information:{} error.", entry.getKey(), entry.getValue());
            iterator.remove();
        }
    }


    /**
     * 数据库的数据回写成功
     * @param primaryDelayCache
     */
    private void onPrimaryDBCacheSuccess(PrimaryDelayCache<K, V> primaryDelayCache){
        List<V> dataList = primaryDelayCache.getAll().stream().map(KeyDataValue::getDataValue).collect(Collectors.toList());
        onReplaceDbBatchSuccess(primaryDelayCache.getPrimaryKey(), dataList, CacheInformation.DEFAULT);
    }


    /**
     * 加载数据库的数据~
     */
    private DataCollection<K, V> getDBCollection(long primaryKey, long currentTime){
        DataCollection<K, V> dataCollection = dbSource.getCollection(primaryKey);
        List<V> dataList = dataCollection.getDataList();
        CacheInformation cacheInformation = dataCollection.getCacheInformation();
        cacheInformation.updateCurrentTime(currentTime);
        onReplaceDbBatchSuccess(primaryKey, dataList, cacheInformation);
        return dataCollection;
    }

    /**
     * 更换数据之前需要做的事情
     * @param values
     */
    private void beforeReplaceBatch(Collection<V> values){
        if (values.isEmpty()){
            return;
        }
        values.forEach(value -> DataPrivilegeUtil.invokeSetBitIndex(value, DataBitIndex.RedisChangeIndex));
    }

    /**
     * 回写Redis数据成功之后
     * @param primaryKey
     * @param values
     * @return
     */
    private boolean onReplaceRedisBatchSuccess(long primaryKey, Collection<V> values){
        if (values.isEmpty()){
            return true;
        }
        boolean success = dbSource.replaceBatch(primaryKey, values);
        if (dbSource instanceof ICacheDelaySource) {
            return true;    //留给回调的时候调用
        }
        else if (success){
            success = onReplaceDbBatchSuccess(primaryKey, values, CacheInformation.DEFAULT);
        }
        return success;
    }

    /**
     * 回写数据库成功之后
     * @param primaryKey
     * @param values
     * @param cacheInformation
     * @return
     */
    private boolean onReplaceDbBatchSuccess(long primaryKey, Collection<V> values, CacheInformation cacheInformation){
        values.forEach(value -> DataPrivilegeUtil.invokeClearBitIndex(value, DataBitIndex.RedisChangeIndex));
        return redisSource.replaceBatch(primaryKey, values, cacheInformation);
    }
}
