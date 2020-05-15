package com.game.cache.data;

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

class PrimaryDataContainer<PK, K, V extends Data<K>> implements IPrimaryDataContainer<PK, K, V>{

    private static final Logger logger = LoggerFactory.getLogger(PrimaryDataContainer.class);

    private final PK primaryKey;
    private ConcurrentHashMap<K, V> key2Values;
    private CacheInformation information;
    private final IDataSource<PK, K, V> dataSource;

    public PrimaryDataContainer(PK primaryKey, IDataSource<PK, K, V> dataSource) {
        this.primaryKey = primaryKey;
        this.key2Values = new ConcurrentHashMap<>();
        this.information = null;
        this.dataSource = dataSource;
    }

    @Override
    public PK primaryKey() {
        return primaryKey;
    }

    @Override
    public int count() {
        return currentMap().size();
    }

    @Override
    public V get(K secondaryKey) {
        return currentMap().get(secondaryKey);
    }

    @Override
    public Collection<V> getAll() {
        return currentMap().values();
    }

    @Override
    public V replaceOne(V value) {
        Args.Two<Boolean, V> resultValue = LockUtil.syncLock(dataSource.getLockKey(), "removeOne", () -> {
            boolean success = dataSource.replaceOne(primaryKey, value);
            V oldValue = null;
            if (success){
                value.clearIndexChangedBits();
                oldValue = currentMap().put(value.secondaryKey(), value);
            }
            return Args.create(success, oldValue);
        });
        if (resultValue != null && resultValue.arg0){
            if (logger.isTraceEnabled()) {
                logger.trace("replaceOne: {}", LogUtil.toJSONString(value));
            }
            return resultValue.arg1;
        }
        else {
            throw new CacheException("primaryKey:%s replaceOne error, %s", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(value));
        }
    }

    @Override
    public void replaceBatch(Collection<V> values) {
        Boolean isSuccess = LockUtil.syncLock(dataSource.getLockKey(), "removeBatch", () -> {
            boolean success = dataSource.replaceBatch(primaryKey, values);
            if (success){
                ConcurrentHashMap<K, V> currentMap = currentMap();
                for (V value : values) {
                    currentMap.put(value.secondaryKey(), value);
                    value.clearIndexChangedBits();
                }
            }
            return success;
        });
        if (isSuccess){
            if (logger.isTraceEnabled()) {
                logger.trace("replaceBatch:{}", LogUtil.toJSONString(values));
            }
        }
        else {
            throw new CacheException("primaryKey:%s replaceBatch error, %s", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(values));
        }
    }

    @Override
    public V removeOne(K secondaryKey) {
        Args.Two<Boolean, V> resultValue = LockUtil.syncLock(dataSource.getLockKey(), "removeOne", () -> {
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
            if (resultValue.arg1 != null && logger.isTraceEnabled()) {
                logger.trace("removeOne: {}", LogUtil.toJSONString(resultValue.arg1));
            }
            return resultValue.arg1;
        }
        else {
            throw new CacheException("primaryKey:%s secondaryKey:%s removeOne error.", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(secondaryKey));
        }
    }

    @Override
    public void removeBatch(Collection<K> secondaryKeys) {
        Boolean isSuccess = LockUtil.syncLock(dataSource.getLockKey(), "removeBatch", () -> {
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
            if (logger.isTraceEnabled()) {
                logger.trace("removeBatch: {}", LogUtil.toJSONString(secondaryKeys));
            }
        }
        else {
            throw new CacheException("primaryKey:%s removeBatch error.", LogUtil.toJSONString(primaryKey));
        }
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
            return key2Values;
        }
        DataCollection<K, V> collection = dataSource.getCollection(primaryKey);
        information = collection.getInformation();
        List<V> valueList = collection.getValueList();
        for (V value : valueList) {
            key2Values.put(value.secondaryKey(), value);
        }
        return key2Values;
    }
}
