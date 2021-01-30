package com.game.core.cache.data;

import com.game.common.util.RandomUtil;
import com.game.core.cache.CacheInformation;
import com.game.core.cache.source.executor.CacheRunnable;
import com.game.core.cache.source.executor.ICacheExecutor;
import com.game.common.util.Holder;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DataContainer<K, V extends IData<K>> implements IDataContainer<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(DataContainer.class);

    private static final CacheInformation INIT_INFO = new CacheInformation();

    private final IDataSource<K, V> dataSource;
    private final IDataLifePredicate loadPredicate;
    private ConcurrentHashMap<Long, IPrimaryDataContainer<K, V>> primaryDataMap;

    public DataContainer(IDataSource<K, V> dataSource, IDataLifePredicate loadPredicate, ICacheExecutor executor) {
        this.dataSource = dataSource;
        this.loadPredicate = loadPredicate;
        this.primaryDataMap = new ConcurrentHashMap<>();
        //初始化~
        String name = "dataContainer." + dataSource.getCacheUniqueId().getName();
        long initialDelay = RandomUtil.nextLong(1000, 2000) / 50;
        executor.scheduleAtFixedRate(new CacheRunnable(name, this::onScheduleAll), initialDelay, 1000L, TimeUnit.MILLISECONDS);
    }


    @Override
    public boolean existCache(long primaryKey) {
        return primaryDataMap.containsKey(primaryKey);
    }

    @Override
    public int count(long primaryKey) {
        return primaryDataContainer(primaryKey).count();
    }

    @Override
    public V get(long primaryKey, K secondaryKey) {
        return  primaryDataContainer(primaryKey).get(secondaryKey);
    }

    @Override
    public Holder<V> getNoCache(long primaryKey, K secondaryKey) {
        IPrimaryDataContainer<K, V> primaryDataContainer = primaryDataMap.get(primaryKey);
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
    public Collection<V> getAll(long primaryKey) {
        return primaryDataContainer(primaryKey).getAll();
    }

    @Override
    public Collection<V> getAllNoCache(long primaryKey) {
        IPrimaryDataContainer<K, V> primaryDataContainer = primaryDataMap.get(primaryKey);
        if (primaryDataContainer == null){
            return null;
        }
        return primaryDataContainer.getAll().stream().map(dataSource::cloneValue).collect(Collectors.toList());
    }

    @Override
    public V replaceOne(long primaryKey, V value) {
        return primaryDataContainer(primaryKey).replaceOne(value);
    }

    @Override
    public void replaceBatch(long primaryKey, Collection<V> values) {
        primaryDataContainer(primaryKey).replaceBatch(values);
    }

    @Override
    public V removeOne(long primaryKey, K secondaryKeys) {
        return primaryDataContainer(primaryKey).removeOne(secondaryKeys);
    }

    @Override
    public void removeBatch(long primaryKey, Collection<K> secondaryKeys) {
        primaryDataContainer(primaryKey).removeBatch(secondaryKeys);
    }

    @Override
    public boolean flushAll(long currentTime) {
        return dataSource.flushAll(currentTime);
    }

    @Override
    public void flushOne(long primaryKey, long currentTime, Consumer<Boolean> consumer) {
        dataSource.flushOne(primaryKey, currentTime, consumer);
    }

    private IPrimaryDataContainer<K, V> primaryDataContainer(long primaryKey){
        return primaryDataMap.computeIfAbsent(primaryKey, key -> new PrimaryDataContainer<>(key, dataSource, loadPredicate));
    }

    private void onScheduleAll(){
        long currentTime = System.currentTimeMillis();
        for (IPrimaryDataContainer<K, V> container : primaryDataMap.values()) {
            try {
                container.onSchedule(currentTime);
            }
            catch (Throwable t){
                logger.error("primaryKey:{} onSchedule error.", container.primaryKey());
            }
        }
    }
}
