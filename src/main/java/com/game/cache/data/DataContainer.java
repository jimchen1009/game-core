package com.game.cache.data;

import com.game.cache.CacheInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DataContainer<PK, K, V extends Data<K>> implements IDataContainer<PK, K, V> {

    private static final Logger logger = LoggerFactory.getLogger(DataContainer.class);

    private static final CacheInformation INIT_INFO = new CacheInformation();

    private ConcurrentHashMap<PK, IPrimaryDataContainer<PK, K, V>> primaryDataMap;
    private final IDataSource<PK, K, V> dataSource;

    public DataContainer(IDataSource<PK, K, V> dataSource) {
        this.primaryDataMap = new ConcurrentHashMap<>();
        this.dataSource = dataSource;
    }

    @Override
    public int count(PK primaryKey) {
        return primaryDataContainer(primaryKey).count();
    }

    @Override
    public V get(PK primaryKey, K key) {
        return get(primaryKey, key, false);
    }

    @Override
    public V get(PK primaryKey, K key, boolean isClone) {
        V value = primaryDataContainer(primaryKey).get(key);
        if (value != null && isClone){
            value = dataSource.cloneValue(value);
        }
        return value;
    }

    @Override
    public Collection<V> getAll(PK primaryKey) {
        return primaryDataContainer(primaryKey).getAll();
    }

    @Override
    public Collection<V> getAll(PK primaryKey, boolean isClone) {
        return getAll(primaryKey).stream().map(dataSource::cloneValue).collect(Collectors.toList());
    }

    @Override
    public V replaceOne(PK primaryKey, V value) {
        return primaryDataContainer(primaryKey).replaceOne(value);
    }

    @Override
    public void replaceBatch(PK primaryKey, Collection<V> values) {
        primaryDataContainer(primaryKey).replaceBatch(values);
    }

    @Override
    public V removeOne(PK primaryKey, K key) {
        return primaryDataContainer(primaryKey).removeOne(key);
    }

    @Override
    public void removeBatch(PK primaryKey, Collection<K> keys) {
        primaryDataContainer(primaryKey).removeBatch(keys);
    }

    private IPrimaryDataContainer<PK, K, V> primaryDataContainer(PK primaryKey){
        return primaryDataMap.computeIfAbsent(primaryKey, key -> new PrimaryDataContainer<>(key, dataSource));
    }
}
