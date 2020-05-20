package com.game.cache.data;

import com.game.cache.CacheInformation;
import com.game.common.util.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DataContainer<PK, K, V extends IData<K>> implements IDataContainer<PK, K, V> {

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
    public V get(PK primaryKey, K secondaryKey) {
        return  primaryDataContainer(primaryKey).get(secondaryKey);
    }

    @Override
    public Holder<V> getNoCache(PK primaryKey, K secondaryKey) {
        IPrimaryDataContainer<PK, K, V> primaryDataContainer = primaryDataMap.get(primaryKey);
        if (primaryDataContainer == null){
            return null;
        }
        V value = primaryDataContainer.get(secondaryKey);
        if (value != null){
            value = dataSource.cloneValue(value);
        }
        return new Holder<>(value);
    }

    @Override
    public Collection<V> getAll(PK primaryKey) {
        return primaryDataContainer(primaryKey).getAll();
    }

    @Override
    public Collection<V> getAllNoCache(PK primaryKey) {
        IPrimaryDataContainer<PK, K, V> primaryDataContainer = primaryDataMap.get(primaryKey);
        if (primaryDataContainer == null){
            return null;
        }
        return primaryDataContainer.getAll().stream().map(dataSource::cloneValue).collect(Collectors.toList());
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
    public V removeOne(PK primaryKey, K secondaryKeys) {
        return primaryDataContainer(primaryKey).removeOne(secondaryKeys);
    }

    @Override
    public void removeBatch(PK primaryKey, Collection<K> secondaryKeys) {
        primaryDataContainer(primaryKey).removeBatch(secondaryKeys);
    }

    private IPrimaryDataContainer<PK, K, V> primaryDataContainer(PK primaryKey){
        return primaryDataMap.computeIfAbsent(primaryKey, key -> new PrimaryDataContainer<>(key, dataSource));
    }
}
