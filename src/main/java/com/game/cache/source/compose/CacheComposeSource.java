package com.game.cache.source.compose;

import com.game.cache.CacheInformation;
import com.game.cache.CacheName;
import com.game.cache.CacheType;
import com.game.cache.ICacheDaoUnique;
import com.game.cache.data.DataBitIndex;
import com.game.cache.data.DataCollection;
import com.game.cache.data.IData;
import com.game.cache.mapper.ClassInformation;
import com.game.cache.mapper.IClassConverter;
import com.game.cache.source.ICacheDelaySource;
import com.game.cache.source.ICacheKeyValueBuilder;
import com.game.cache.source.KeyDataValue;
import com.game.cache.source.PrimaryDelayCache;
import com.game.cache.source.executor.ICacheExecutor;
import com.game.cache.source.executor.ICacheSource;
import com.game.cache.source.redis.ICacheRedisSource;
import com.game.common.lock.LockKey;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 删除的操作还没有实现~
 * @param <K>
 * @param <V>
 */
public class CacheComposeSource<K, V extends IData<K>> implements ICacheComposeSource<K, V> {

    private final ICacheRedisSource<K, V> redisSource;
    private final ICacheSource<K, V> dbSource;

    public CacheComposeSource(ICacheRedisSource<K, V> redisSource, ICacheSource<K, V> dbSource) {
        this.redisSource = redisSource;
        this.dbSource = dbSource;
        if (dbSource instanceof ICacheDelaySource){
            ((ICacheDelaySource<K, V>)dbSource).addFlushCallback(this::onPrimaryFlushSuccess);
        }
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
        DataCollection<K, V> collection = redisSource.getCollection(primaryKey);
        if (collection == null || collection.isEmpty()){
            collection = dbSource.getCollection(primaryKey);
            List<V> dataList = collection.getDataList();
            CacheInformation cacheInformation = new CacheInformation();
            cacheInformation.setValue(CacheName.DATA_EMPTY, "EMPTY");
            flushBatchSuccess(primaryKey, dataList, cacheInformation);
        }
        else {
            List<V> changeDataList = collection.getDataList().stream().filter(data -> data.hasBitIndex(DataBitIndex.RedisChanged)).collect(Collectors.toList());
            flushBatchSuccess(primaryKey, changeDataList, null);
        }
        return collection;
    }

    @Override
    public boolean replaceOne(long primaryKey, V value) {
        replaceBatchBefore(Collections.singleton(value));
        boolean isSuccess = redisSource.replaceOne(primaryKey, value);
        if (isSuccess){
            isSuccess = replaceBatchSuccess(primaryKey, Collections.singleton(value));
        }
        return isSuccess;
    }

    @Override
    public boolean replaceBatch(long primaryKey, Collection<V> values) {
        replaceBatchBefore(values);
        boolean isSuccess = redisSource.replaceBatch(primaryKey, values);
        if (isSuccess){
            isSuccess = replaceBatchSuccess(primaryKey, values);
        }
        return isSuccess;
    }

    private void replaceBatchBefore(Collection<V> values){
        if (values.isEmpty()){
            return;
        }
        ClassInformation information = getConverter().getInformation();
        values.forEach(value -> information.invokeSetBitIndex(value, DataBitIndex.RedisChanged));
    }

    private boolean replaceBatchSuccess(long primaryKey, Collection<V> values){
        if (values.isEmpty()){
            return true;
        }
        boolean success = dbSource.replaceBatch(primaryKey, values);
        if (dbSource instanceof ICacheDelaySource) {
            return true;    //留给回调的时候调用
        }
        else if (success){
            success = flushBatchSuccess(primaryKey, values, null);
        }
        return success;
    }

    private boolean flushBatchSuccess(long primaryKey, Collection<V> values, CacheInformation cacheInformation){
        if (values.isEmpty()){
            return true;
        }
        ClassInformation information = getConverter().getInformation();
        values.forEach(value -> information.invokeClearBitIndex(value, DataBitIndex.RedisChanged));
        return redisSource.replaceBatch(primaryKey, values, cacheInformation);
    }

    @Override
    public Class<V> getAClass() {
        return redisSource.getAClass();
    }


    @Override
    public ICacheDaoUnique getCacheDaoUnique() {
        return redisSource.getCacheDaoUnique();
    }

    @Override
    public CacheType getCacheType() {
       throw new UnsupportedOperationException();
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
        dbSource.flushOne(primaryKey, currentTime, consumer);
    }

    @Override
    public ICacheKeyValueBuilder<K> getKeyValueBuilder() {
        return redisSource.getKeyValueBuilder();
    }

    @Override
    public ICacheDelaySource<K, V> createDelayUpdateSource(ICacheExecutor executor) {
        throw new UnsupportedOperationException();
    }

    private void onPrimaryFlushSuccess(PrimaryDelayCache<K, V> primaryDelayCache){
        ClassInformation information = getConverter().getInformation();
        List<V> dataList = primaryDelayCache.getAll().stream().map(KeyDataValue::getDataValue).collect(Collectors.toList());
        flushBatchSuccess(primaryDelayCache.getPrimaryKey(), dataList, null);
    }
}
