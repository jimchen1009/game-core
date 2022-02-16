package com.game.core.cache.data;

import com.game.common.arg.Args;
import com.game.common.log.LogUtil;
import com.game.core.cache.exception.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PrimaryDataContainer<K, V extends IData<K>> implements IPrimaryDataContainer<K, V>{

    private static final Logger logger = LoggerFactory.getLogger(PrimaryDataContainer.class);

    private final long primaryKey;
    private ConcurrentHashMap<K, V> secondary2Values;
    private final IDataSource<K, V> dataSource;
    private final IDataLifePredicate lifePredicate;

    public PrimaryDataContainer(long primaryKey, IDataSource<K, V> dataSource, IDataLifePredicate lifePredicate) {
        this.primaryKey = primaryKey;
        this.secondary2Values = null;
        this.dataSource = dataSource;
        this.lifePredicate = lifePredicate;
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
        Args.Two<Boolean, V> resultValue = DataSourceUtil.syncLock(dataSource, primaryKey, "deleteOne", () -> {
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
        Boolean isSuccess = DataSourceUtil.syncLock(dataSource, primaryKey, "deleteBatch", () -> {
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
        Args.Two<Boolean, V> resultValue = DataSourceUtil.syncLock(dataSource, primaryKey, "deleteOne", () -> {
            ConcurrentHashMap<K, V> currentMap = currentMap();
            boolean success = dataSource.deleteOne(primaryKey, secondaryKey);
            V data = null;
            if (success){
                data = currentMap.remove(secondaryKey);
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
        Boolean isSuccess = DataSourceUtil.syncLock(dataSource, primaryKey, "deleteBatch", () -> {
            ConcurrentHashMap<K, V> currentMap = currentMap();
            boolean success = dataSource.deleteBatch(primaryKey, secondaryKeys);
            if (success) {
                secondaryKeys.forEach(currentMap::remove);
            }
            return success;
        });
        if (isSuccess != null && isSuccess){
        }
        else {
            throw new CacheException("primaryKey:%s deleteBatch error, %s", LogUtil.toJSONString(primaryKey), LogUtil.toJSONString(secondaryKeys));
        }
    }

    private ConcurrentHashMap<K, V> lockCurrentMap(){
        ConcurrentHashMap<K, V> currentMap = DataSourceUtil.syncLock(dataSource, primaryKey, "currentMap", this::currentMap);
        if (currentMap == null){
            throw new CacheException("primaryKey:%s load cache exception.", LogUtil.toJSONString(primaryKey));
        }
        return currentMap;
    }

    private ConcurrentHashMap<K, V> currentMap(){
        if (secondary2Values == null){
            if (lifePredicate.compareAndUpdate(primaryKey, dataSource.getCacheUniqueId())){
                secondary2Values = new ConcurrentHashMap<>();
            }
            else {
                secondary2Values = new ConcurrentHashMap<>();
                DataCollection<K, V> collection = dataSource.getCollection(primaryKey);
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
