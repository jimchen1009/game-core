package com.game.core.cache.source.compose;

import com.game.common.lock.LockKey;
import com.game.common.util.CommonUtil;
import com.game.core.cache.CacheType;
import com.game.core.cache.ICacheUniqueId;
import com.game.core.cache.data.DataBitIndex;
import com.game.core.cache.data.DataCollection;
import com.game.core.cache.data.DataPrivilegeUtil;
import com.game.core.cache.data.IData;
import com.game.core.cache.mapper.IClassConverter;
import com.game.core.cache.source.CacheCommand;
import com.game.core.cache.source.ICacheDelaySource;
import com.game.core.cache.source.ICacheKeyValueBuilder;
import com.game.core.cache.source.KeyDataCommand;
import com.game.core.cache.source.PrimaryDelayCache;
import com.game.core.cache.source.executor.ICacheExecutor;
import com.game.core.cache.source.redis.ICacheRedisSource;
import com.game.core.cache.source.redis.RedisCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 *
 * @param <K>
 * @param <V>
 */
public class RedisDbSource<K, V extends IData<K>> implements IRedisDbSource<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(RedisDbSource.class);

    private final ICacheRedisSource<K, V> redisSource;
    private final ICacheDelaySource<K, V> dbSource;
    private BiFunction<Long, K, V> function;

    public RedisDbSource(ICacheRedisSource<K, V> redisSource, ICacheDelaySource<K, V> dbSource) {
        this.redisSource = redisSource;
        this.dbSource = dbSource;
        dbSource.setWriteBackCallback(this::onWriteBackPrimaryDataSuccess);
    }

    @Override
    public CacheType getCacheType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ICacheExecutor getExecutor() {
        return redisSource.getExecutor();
    }

    @Override
    public LockKey getLockKey(long primaryKey) {
        return redisSource.getLockKey(primaryKey);
    }

    @Override
    public V get(long primaryKey, K secondaryKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<V> getAll(long primaryKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataCollection<K, V> getCollection(long primaryKey) {
        /***
         * 获取用户数据: 用户自己 与 其他用户 都会获取数据, 存在并发情况
         * 1. 不用跨进程锁
         * 2. 使用过期时间规避【过期时间必须尽可能早的被更新到】
         */
        RedisCollection<K, V> redisCollection = redisSource.getCollection(primaryKey);
        long currentTime = System.currentTimeMillis();
        if (redisCollection.isExpired(currentTime)) {
            // 如果数据过期了, 那么之前没有回写的数据只能回档放弃了
            DataCollection<K, V> dataCollection = dbSource.getCollection(primaryKey);
            redisSource.replaceBatch(primaryKey, dataCollection);
            return dataCollection;
        }
        else {
            Map<Integer, List<V>> up1Into1Group = CommonUtil.splitUp1Into1Group(new HashMap<>(), redisCollection.getDataList(), ArrayList::new, data -> {
                if (data.isDeleted()) {
                    return CacheCommand.DELETE.getId();
                }
                else if (DataPrivilegeUtil.existIndexBit(data, DataBitIndex.RedisUpdate)) {
                    return CacheCommand.UPSERT.getId();
                }
                else {
                    return 0;
                }
            });
            List<K> secondaryKeys = up1Into1Group.remove(CacheCommand.DELETE.getId()).stream().map(IData::secondaryKey).collect(Collectors.toList());
            List<V> dataList = up1Into1Group.getOrDefault(CacheCommand.UPSERT.getId(), Collections.emptyList());
            onSyncRedisUpdateToDbSuccess(primaryKey, secondaryKeys, dataList);
            List<V> newDataList = new ArrayList<>(dataList);
            newDataList.addAll(up1Into1Group.getOrDefault(0, Collections.emptyList()));
            return new DataCollection<>(newDataList);
        }
    }

    @Override
    public boolean replaceOne(long primaryKey, V data) {
        beforeRedisUpdateBatch(Collections.singleton(data));
        boolean isSuccess = redisSource.replaceOne(primaryKey, data);
        if (isSuccess){
            isSuccess = syncRedisUpdateToDb(primaryKey, Collections.emptyList(), Collections.singleton(data));
        }
        return isSuccess;
    }

    @Override
    public boolean replaceBatch(long primaryKey, Collection<V> dataList) {
        beforeRedisUpdateBatch(dataList);
        boolean isSuccess = redisSource.replaceBatch(primaryKey, dataList);
        if (isSuccess){
            isSuccess = syncRedisUpdateToDb(primaryKey, Collections.emptyList(), dataList);
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
        V data = function.apply(primaryKey, secondaryKey);
        if (data == null || data.isDeleted()){
            return true;
        }
        if (!data.delete(System.currentTimeMillis())) {
            throw new UnsupportedOperationException();
        }
        return this.replaceOne(primaryKey, data);
    }

    @Override
    public boolean deleteBatch(long primaryKey, Collection<K> secondaryKeys) {
        List<V> dataList = secondaryKeys.stream().map(secondaryKey -> function.apply(primaryKey, secondaryKey))
                .filter(Objects::nonNull).filter(data -> !data.isDeleted()).collect(Collectors.toList());
        for (V data : dataList) {
            if (!data.delete(System.currentTimeMillis())) {
                throw new UnsupportedOperationException();
            }
        }
        return this.replaceBatch(primaryKey, dataList);
    }

    @Override
    public V cloneValue(V data) {
        return dbSource.cloneValue(data);
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
    public ICacheDelaySource<K, V> createDelayUpdateSource() {
       throw new UnsupportedOperationException();
    }

    @Override
    public void setGetDataFunction(BiFunction<Long, K, V> function) {
        this.function = function;
    }

    private void beforeRedisUpdateBatch(Collection<V> dataList){
        dataList.forEach(value -> DataPrivilegeUtil.setIndexBit(value, DataBitIndex.RedisUpdate));
    }

    private boolean syncRedisUpdateToDb(long primaryKey, Collection<K> secondaryKeys, Collection<V> dataList){
        if (dataList.isEmpty()){
            return true;
        }
        boolean success = dbSource.replaceBatch(primaryKey, dataList);
        success &= dbSource.deleteBatch(primaryKey, secondaryKeys);
        return success;
    }

    private boolean onSyncRedisUpdateToDbSuccess(long primaryKey, Collection<K> secondaryKeys, Collection<V> dataList){
        dataList.forEach(data -> DataPrivilegeUtil.clearIndexBit(data, DataBitIndex.RedisUpdate));
        return redisSource.replaceBatch(primaryKey, secondaryKeys, dataList);
    }

    /**
     * 数据库的数据回写成功回调
     * 1.1. 由于是异步线程，那么回调的数据是旧数据
     * 1.2. 旧数据不能直接覆盖新的数, redis不能直接回写
     * 2.1. 由于是异步线程，那么异步与用户线程存在并发，需要处理
     * 2.2. 只能使用锁，同步线程
     */
    private void onWriteBackPrimaryDataSuccess(PrimaryDelayCache<K, V> primaryDelayCache){
        Map<CacheCommand, List<KeyDataCommand<K, V>>> into1Group = CommonUtil.splitUp1Into1Group(new HashMap<>(), primaryDelayCache.getAllDataCommands(), ArrayList::new, KeyDataCommand::getCommand);
        List<K> secondaryKeys = into1Group.getOrDefault(CacheCommand.DELETE, Collections.emptyList()).stream().map(KeyDataCommand::getKey).collect(Collectors.toList());
        List<V> dataList = into1Group.getOrDefault(CacheCommand.UPSERT, Collections.emptyList()).stream().map(KeyDataCommand::getData).collect(Collectors.toList());
        onSyncRedisUpdateToDbSuccess(primaryDelayCache.getPrimaryKey(), secondaryKeys, dataList);
    }
}
