package com.game.core.cache.data;

import com.game.common.config.EvnCoreConfigs;
import com.game.common.config.EvnCoreType;
import com.game.common.config.IEvnConfig;
import com.game.common.lock.LockUtil;
import com.game.common.util.Holder;
import com.game.core.cache.exception.CacheException;
import com.game.core.cache.source.executor.ICacheSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.Callable;

public class DataSourceUtil {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceUtil.class);


    public static <K, V extends IData<K>> IDataSource<K, V> createDataSource(ICacheSource<K, V> cacheSource){
        IDataSource<K, V> dataSource = new DataSource<>(cacheSource);
        IEvnConfig dataConfig = EvnCoreConfigs.getInstance(EvnCoreType.CACHE).getConfig("data");
        List<String> decorators = dataConfig.getList("decorators");
        for (String decorator : decorators) {
            String className = DataSource.class.getName() + decorator.toUpperCase().charAt(0) + decorator.toLowerCase().substring(1);
            try {
                logger.info("initialize decorator: {}", className);
                Class<?> decoratorClass = Class.forName(className);
                Constructor<?> constructor = decoratorClass.getConstructor(IDataSource.class);
                constructor.setAccessible(true);
                //noinspection unchecked
                dataSource =  (IDataSource<K, V>)constructor.newInstance(dataSource);
                logger.info("finish decorator: {}", className);
            }
            catch (Throwable t) {
                logger.error("decorator error, decorator is {}", className, t);
            }
        }
        return dataSource;
    }

    public static <K, V extends IData<K>, T> T syncLock(IDataSource<K, V> dataSource, long primaryKey, String message, Callable<T> callable){
        return LockUtil.syncLock(dataSource.getLockKey(primaryKey), message, callable);
    }

    public static <K, V extends IData<K>> V getNotCache(IDataSource<K, V> dataSource, long primaryKey, K secondaryKey) {
        Holder<V> holder = syncLock(dataSource, primaryKey, "getNotCache", () -> new Holder<>(dataSource.get(primaryKey, secondaryKey)));
        if (holder == null){
            throw new CacheException("primaryKey:%s secondaryKey:%s getNotCache error", primaryKey, secondaryKey);
        }
        return holder.getValue();
    }

    public static <K, V extends IData<K>> List<V> getAllNotCache(IDataSource<K, V> dataSource, long primaryKey) {
        List<V> values = syncLock(dataSource, primaryKey, "getAllNotCache", () -> dataSource.getAll(primaryKey));
        if (values == null) {
            throw new CacheException("primaryKey:%s getAllNotCache error", primaryKey);
        }
        return values;
    }
}
