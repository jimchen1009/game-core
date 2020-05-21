package com.game.cache.data;

import com.game.cache.CacheContext;
import com.game.cache.CacheInformation;
import com.game.cache.exception.CacheException;
import com.game.common.arg.Args;
import com.game.common.lock.LockUtil;
import com.game.common.log.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PrimaryDataContainer<PK, K, V extends IData<K>> implements IPrimaryDataContainer<PK, K, V>{

    private static final Logger logger = LoggerFactory.getLogger(PrimaryDataContainer.class);

    private final PK primaryKey;
    private ConcurrentHashMap<K, V> secondary2Values;
    private CacheInformation information;
    private final IDataSource<PK, K, V> dataSource;
    private final IDataLoadPredicate<PK> loadPredicate;
    private volatile long latestUpdateTime;

    public PrimaryDataContainer(PK primaryKey, IDataSource<PK, K, V> dataSource, IDataLoadPredicate<PK> loadPredicate) {
        this.primaryKey = primaryKey;
        this.secondary2Values = new ConcurrentHashMap<>();
        this.information = null;
        this.dataSource = dataSource;
        this.loadPredicate = loadPredicate;
        this.latestUpdateTime = CacheContext.getCurrentTime();
    }

    @Override
    public PK primaryKey() {
        return primaryKey;
    }

    @Override
    public int count() {
        return lockCurrentMap().size();
    }

    @Override
    public V get(K secondaryKey) {
        return lockCurrentMap().get(secondaryKey);
    }

    @Override
    public Collection<V> getAll() {
        return lockCurrentMap().values();
    }

    @Override
    public V replaceOne(V value) {
        Args.Two<Boolean, V> resultValue = LockUtil.syncLock(dataSource.getLockKey(), "deleteOne", () -> {
            boolean success = dataSource.replaceOne(primaryKey, value);
            V oldValue = null;
            if (success){
                oldValue = currentMap().put(value.secondaryKey(), value);
                value.clearIndexChangedBits();
            }
            return Args.create(success, oldValue);
        });
        if (resultValue != null && resultValue.arg0){
            return resultValue.arg1;
        }
        else {
            throw new CacheException("primaryKey:%s replaceOne error, %s", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(value));
        }
    }

    @Override
    public void replaceBatch(Collection<V> values) {
        Boolean isSuccess = LockUtil.syncLock(dataSource.getLockKey(), "deleteBatch", () -> {
            boolean success = dataSource.replaceBatch(primaryKey, values);
            if (success){
                ConcurrentHashMap<K, V> currentMap = currentMap();
                for (V value : values) {
                    value.clearIndexChangedBits();
                    currentMap.put(value.secondaryKey(), value);
                }
            }
            return success;
        });
        if (isSuccess){
        }
        else {
            throw new CacheException("primaryKey:%s replaceBatch error, %s", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(values));
        }
    }

    @Override
    public V removeOne(K secondaryKey) {
        Args.Two<Boolean, V> resultValue = LockUtil.syncLock(dataSource.getLockKey(), "deleteOne", () -> {
            boolean success = true;
            V oldValue = null;
            if (currentMap().containsKey(secondaryKey)) {
                success = dataSource.deleteOne(primaryKey, secondaryKey);
                if (success){
                    oldValue = currentMap().remove(secondaryKey);
                }
            }
            return Args.create(success, oldValue);
        });
        if (resultValue != null && resultValue.arg0){
            return resultValue.arg1;
        }
        else {
            throw new CacheException("primaryKey:%s deleteOne error, %s", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(secondaryKey));
        }
    }

    @Override
    public void removeBatch(Collection<K> secondaryKeys) {
        Boolean isSuccess = LockUtil.syncLock(dataSource.getLockKey(), "deleteBatch", () -> {
            boolean success = true;
            ConcurrentHashMap<K, V> currentMap = currentMap();
            List<K> removeSecondaryKeys = secondaryKeys.stream().filter(currentMap::containsKey).collect(Collectors.toList());
            if (!removeSecondaryKeys.isEmpty()){
                success = dataSource.deleteBatch(primaryKey, secondaryKeys);
                if (success){
                    for (K secondaryKey : removeSecondaryKeys) {
                        currentMap.remove(secondaryKey);
                    }
                }
            }
            return success;
        });
        if (isSuccess){
        }
        else {
            throw new CacheException("primaryKey:%s deleteBatch error, %s", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(secondaryKeys));
        }
    }

    @Override
    public long getLatestUpdateTime() {
        return latestUpdateTime;
    }

    @Override
    public void updateLatestUpdateTime() {
        latestUpdateTime = CacheContext.getCurrentTime();
    }

    private ConcurrentHashMap<K, V> lockCurrentMap(){
        ConcurrentHashMap<K, V> currentMap = LockUtil.syncLock(dataSource.getLockKey(), "currentMap", this::currentMap);
        if (currentMap == null){
            throw new CacheException("primaryKey:%s load cache time out.", LogUtil.toJSONString(primaryKey));
        }
        return currentMap;
    }

    private ConcurrentHashMap<K, V> currentMap(){
        if (information != null) {
            return secondary2Values;
        }
        if (loadPredicate.predicateNoCache(primaryKey)){
            information = new CacheInformation();
            loadPredicate.onPredicateCacheLoaded(primaryKey);
        }
        else {
            DataCollection<K, V> collection = dataSource.getCollection(primaryKey);
            information = collection.getInformation();
            List<V> valueList = collection.getValueList();
            for (V value : valueList) {
                secondary2Values.put(value.secondaryKey(), value);
            }
        }
        return secondary2Values;
    }
}
