package com.game.cache.data;

import com.game.cache.source.ICacheDelaySource;
import com.game.cache.source.executor.ICacheSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class DataSourceBuilder<K, V extends IData<K>> {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceBuilder.class);

    private ICacheSource<K, V> cacheSource;

    private List<String> decorators;

    public DataSourceBuilder(ICacheSource<K, V> cacheSource) {
        this.cacheSource = cacheSource;
        this.decorators = new ArrayList<>();
    }


    public DataSourceBuilder<K, V> setCacheSource(ICacheSource<K, V> cacheSource) {
        this.cacheSource = cacheSource;
        return this;
    }

    public DataSourceBuilder<K, V> setDecorators(List<String> decorators) {
        this.decorators = decorators;
        return this;
    }

    public IDataSource<K, V> create(){
        return createDataSource(cacheSource);
    }

    public IDataSource<K, V> createNoDelay(){
        ICacheSource<K, V> cacheSource = this.cacheSource;
        while (cacheSource instanceof ICacheDelaySource){
            cacheSource = ((ICacheDelaySource<K, V>) cacheSource).getCacheSource();
        }
        return createDataSource(cacheSource);
    }

    private  IDataSource<K, V> createDataSource(ICacheSource<K, V> cacheSource){
        IDataSource<K, V> dataSource = new DataSource<>(cacheSource);
        for (String decorator : decorators) {
            String className = DataSource.class.getName() + decorator.toUpperCase().charAt(0) + decorator.toLowerCase().substring(1);
            try {
                logger.info("init decorator: {}", className);
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
}
