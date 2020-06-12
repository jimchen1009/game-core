package com.game.cache.source.compose;

import com.game.cache.CacheType;
import com.game.cache.data.DataBitIndex;
import com.game.cache.data.DataCollection;
import com.game.cache.data.IData;
import com.game.cache.mapper.ClassConfig;
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
import java.util.stream.Collectors;

/**
 * 删除的操作还没有实现~
 * @param <PK>
 * @param <K>
 * @param <V>
 */
public class CacheComposeSource<PK, K, V extends IData<K>> implements ICacheSource<PK, K, V> {

    private final ICacheRedisSource<PK, K, V> redisSource;
    private final ICacheSource<PK, K, V> dbSource;

    public CacheComposeSource(ICacheRedisSource<PK, K, V> redisSource, ICacheSource<PK, K, V> dbSource) {
        this.redisSource = redisSource;
        this.dbSource = dbSource;
        if (dbSource instanceof ICacheDelaySource){
            ((ICacheDelaySource<PK, K, V>)dbSource).addFlushCallback(this::onPrimaryFlushSuccess);
        }
    }

    @Override
    public LockKey getLockKey(PK primaryKey) {
        return redisSource.getLockKey(primaryKey);
    }

    @Override
    public V get(PK primaryKey, K secondaryKey) {
        V data = redisSource.get(primaryKey, secondaryKey);
        if (data == null){
            data = dbSource.get(primaryKey, secondaryKey);
        }
        return data;
    }

    @Override
    public List<V> getAll(PK primaryKey) {
        List<V> dataList = redisSource.getAll(primaryKey);
        if (dataList == null){
            dataList = dbSource.getAll(primaryKey);
        }
        return dataList;
    }

    @Override
    public DataCollection<K, V> getCollection(PK primaryKey) {
        DataCollection<K, V> collection = redisSource.getCollection(primaryKey);
        if (collection == null || collection.isEmpty()){
            collection = dbSource.getCollection(primaryKey);
        }
        else {
            List<V> changeDataList = collection.getDataList().stream().filter(data -> data.hasBitIndex(DataBitIndex.RedisChanged)).collect(Collectors.toList());
            flushBatchSuccess(primaryKey, changeDataList);
        }
        return collection;
    }

    @Override
    public boolean replaceOne(PK primaryKey, V value) {
        replaceBatchBefore(Collections.singleton(value));
        boolean isSuccess = redisSource.replaceOne(primaryKey, value);
        if (isSuccess){
            isSuccess = replaceBatchSuccess(primaryKey, Collections.singleton(value));
        }
        return isSuccess;
    }

    @Override
    public boolean replaceBatch(PK primaryKey, Collection<V> values) {
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

    private boolean replaceBatchSuccess(PK primaryKey, Collection<V> values){
        if (values.isEmpty()){
            return true;
        }
        boolean success = dbSource.replaceBatch(primaryKey, values);
        if (dbSource instanceof ICacheDelaySource) {
            return true;    //留给回调的时候调用
        }
        else if (success){
            success = flushBatchSuccess(primaryKey, values);
        }
        return success;
    }

    private boolean flushBatchSuccess(PK primaryKey, Collection<V> values){
        if (values.isEmpty()){
            return true;
        }
        ClassInformation information = getConverter().getInformation();
        values.forEach(value -> information.invokeClearBitIndex(value, DataBitIndex.RedisChanged));
        return redisSource.replaceBatch(primaryKey, values);
    }

    @Override
    public Class<V> getAClass() {
        return redisSource.getAClass();
    }

    @Override
    public ClassConfig getClassConfig() {
        return redisSource.getClassConfig();
    }

    @Override
    public CacheType getCacheType() {
       throw new UnsupportedOperationException();
    }

    @Override
    public boolean deleteOne(PK primaryKey, K secondaryKey) {
        boolean isSuccess = redisSource.deleteOne(primaryKey, secondaryKey);
        if (isSuccess){
            isSuccess = dbSource.deleteOne(primaryKey, secondaryKey);
        }
        return isSuccess;
    }

    @Override
    public boolean deleteBatch(PK primaryKey, Collection<K> secondaryKeys) {
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
    public ICacheKeyValueBuilder<PK, K> getKeyValueBuilder() {
        return redisSource.getKeyValueBuilder();
    }

    @Override
    public ICacheDelaySource<PK, K, V> createDelayUpdateSource(ICacheExecutor executor) {
        throw new UnsupportedOperationException();
    }

    private void onPrimaryFlushSuccess(PrimaryDelayCache<PK, K, V> primaryDelayCache){
        ClassInformation information = getConverter().getInformation();
        List<V> dataList = primaryDelayCache.getAll().stream().map(KeyDataValue::getDataValue).collect(Collectors.toList());
        flushBatchSuccess(primaryDelayCache.getPrimaryKey(), dataList);
    }
}
