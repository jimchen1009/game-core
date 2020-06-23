package com.game.cache.data;

import com.game.cache.CacheContext;
import com.game.cache.CacheInformation;
import com.game.cache.exception.CacheException;
import com.game.common.arg.Args;
import com.game.common.lock.LockUtil;
import com.game.common.log.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PrimaryDataContainer<K, V extends IData<K>> implements IPrimaryDataContainer<K, V>{

    private static final Logger logger = LoggerFactory.getLogger(PrimaryDataContainer.class);

    private final long primaryKey;
    private ConcurrentHashMap<K, V> secondary2Values;
    private CacheInformation information;
    private final IDataSource<K, V> dataSource;
    private final IDataLifePredicate loadPredicate;
    private volatile long latestUpdateTime;

    public PrimaryDataContainer(long primaryKey, IDataSource<K, V> dataSource, IDataLifePredicate loadPredicate) {
        this.primaryKey = primaryKey;
        this.secondary2Values = new ConcurrentHashMap<>();
        this.information = null;
        this.dataSource = dataSource;
        this.loadPredicate = loadPredicate;
        this.latestUpdateTime = CacheContext.getCurrentTime();
    }

    @Override
    public long primaryKey() {
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
        Args.Two<Boolean, V> resultValue = LockUtil.syncLock(dataSource.getLockKey(primaryKey), "deleteOne", () -> {
            boolean success = dataSource.replaceOne(primaryKey, value);
            V oldValue = null;
            if (success){
                oldValue = currentMap().put(value.secondaryKey(), value);
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
        Boolean isSuccess = LockUtil.syncLock(dataSource.getLockKey(primaryKey), "deleteBatch", () -> {
            boolean success = dataSource.replaceBatch(primaryKey, values);
            if (success){
                ConcurrentHashMap<K, V> currentMap = currentMap();
                for (V value : values) {
                    currentMap.put(value.secondaryKey(), value);
                }
            }
            return success;
        });
        if (isSuccess != null && isSuccess){

        }
        else {
            throw new CacheException("primaryKey:%s replaceBatch error, %s", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(values));
        }
    }

    @Override
    public V removeOne(K secondaryKey) {
        Args.Two<Boolean, V> resultValue = LockUtil.syncLock(dataSource.getLockKey(primaryKey), "deleteOne", () -> {
            boolean success = true;
            ConcurrentHashMap<K, V> currentMap = currentMap();
            V data = currentMap.get(secondaryKey);
            if (data != null) {
                data.delete(System.currentTimeMillis());
                success = dataSource.replaceOne(primaryKey, data);
                if (success){
                    currentMap.remove(secondaryKey);
                }
            }
            return Args.create(success, data);
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
        Boolean isSuccess = LockUtil.syncLock(dataSource.getLockKey(primaryKey), "deleteBatch", () -> {
            boolean success = true;
            long currentTime = System.currentTimeMillis();
            ConcurrentHashMap<K, V> currentMap = currentMap();
            List<V> dataList = new ArrayList<>();
            for (K secondaryKey : secondaryKeys) {
                V data = currentMap.get(secondaryKey);
                if (data == null){
                    continue;
                }
                data.delete(currentTime);
                dataList.add(data);
            }
            if (!dataList.isEmpty()){
                success = dataSource.replaceBatch(primaryKey, dataList);
                if (success) {
                    for (V data : dataList) {
                        currentMap.remove(data.secondaryKey());
                    }
                }
            }
            return success;
        });
        if (isSuccess != null && isSuccess){
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
        ConcurrentHashMap<K, V> currentMap = LockUtil.syncLock(dataSource.getLockKey(primaryKey), "currentMap", this::currentMap);
        if (currentMap == null){
            throw new CacheException("primaryKey:%s load cache exception.", LogUtil.toJSONString(primaryKey));
        }
        return currentMap;
    }

    private ConcurrentHashMap<K, V> currentMap(){
        long currentTime = System.currentTimeMillis();
        if (information == null || information.isExpired(currentTime)){
            if (loadPredicate.isNewLife(primaryKey)){
                information = new CacheInformation();
                information.updateExpiredTime(currentTime);
                loadPredicate.setOldLife(primaryKey);
            }
            else {
                DataCollection<K, V> collection = dataSource.getCollection(primaryKey);
                information = collection.getInformation();
                List<V> valueList = collection.getDataList();
                for (V value : valueList) {
                    if (value.isDeleted()){
                        continue;
                    }
                    secondary2Values.put(value.secondaryKey(), value);
                }
            }
        }
        return secondary2Values;
    }
}
