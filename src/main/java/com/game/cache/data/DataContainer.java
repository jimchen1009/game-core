package com.game.cache.data;

import com.game.cache.CollectionInfo;
import com.game.cache.exception.CacheException;
import com.game.common.log.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DataContainer<PK, K, V extends Data<K>> implements IDataContainer<PK, K, V> {

    private static final Logger logger = LoggerFactory.getLogger(DataContainer.class);

    private static final CollectionInfo INIT_INFO = new CollectionInfo();

    private final PK primaryKey;
    private ConcurrentHashMap<K, V> valueMap;
    private CollectionInfo information;
    private final IDataSource<PK, K, V> dataSource;

    public DataContainer(PK primaryKey, IDataSource<PK, K, V> dataSource) {
        this.primaryKey = primaryKey;
        this.valueMap = new ConcurrentHashMap<>();
        this.information = INIT_INFO;
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
    public V get(K key) {
        return get(key, false);
    }

    @Override
    public V get(K key, boolean isClone) {
        V value = currentMap().get(key);
        if (value != null && isClone){
            value = dataSource.cloneValue(value);
        }
        return value;
    }

    @Override
    public Collection<V> getAll() {
        return currentMap().values();
    }

    @Override
    public Collection<V> getAll(boolean isClone) {
        return getAll().stream().map(dataSource::cloneValue).collect(Collectors.toList());
    }

    @Override
    public V replaceOne(V value) {
        boolean success = dataSource.replaceOne(primaryKey, value);
        if (success){
            V oldValue = currentMap().put(value.secondaryKey(), value);
            value.clearIndexChangedBits();
            if (logger.isTraceEnabled()) {
                logger.trace("replaceOne: {}", LogUtil.toJSONString(value));
            }
            return oldValue;
        }
        else {
            throw new CacheException("replaceOne error, %s", LogUtil.toJSONString(value));
        }
    }

    @Override
    public void replaceBatch(Collection<V> values) {
        boolean success = dataSource.replaceBatch(primaryKey, values);
        if (success){
            ConcurrentHashMap<K, V> currentMap = currentMap();
            for (V value : values) {
                currentMap.put(value.secondaryKey(), value);
                value.clearIndexChangedBits();
            }
            if (logger.isTraceEnabled()) {
                logger.trace("replaceBatch:{}", LogUtil.toJSONString(values));
            }
        }
        else {
            throw new CacheException("replaceBatch error, %s", LogUtil.toJSONString(values));
        }
    }

    @Override
    public V removeOne(K key) {
        boolean success = dataSource.deleteOne(primaryKey, key);
        if (success){
            V oldValue = currentMap().remove(key);
            if (oldValue != null && logger.isTraceEnabled()) {
                logger.trace("removeOne: {}", LogUtil.toJSONString(oldValue));
            }
            return oldValue;
        }
        else {
            throw new CacheException("removeOne error, %s", LogUtil.toJSONString(key));
        }
    }

    @Override
    public void removeBatch(Collection<K> keys) {
        boolean success = dataSource.deleteBatch(primaryKey, keys);
        if (success){
            ConcurrentHashMap<K, V> currentMap = currentMap();
            for (K key : keys) {
                currentMap.remove(key);
            }
            if (logger.isTraceEnabled()) {
                logger.trace("removeBatch: {}", LogUtil.toJSONString(keys));
            }
        }
        else {
            throw new CacheException("removeBatch error, %s", LogUtil.toJSONString(keys));
        }
    }

    private ConcurrentHashMap<K, V> currentMap(){
        if (INIT_INFO != information){
            return valueMap;
        }
        DataCollection<K, V> collection = dataSource.getCollection(primaryKey);
        information = collection.getInformation();
        List<V> valueList = collection.getValueList();
        for (V value : valueList) {
//            value.setCacheCreated(true);
            valueMap.put(value.secondaryKey(), value);
        }
        return valueMap;
    }
}
