package com.game.cache.data;

import com.game.cache.mapper.ValueConvertMapper;
import com.game.cache.source.executor.ICacheSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

public class DataSourceBuilder<PK, K, V extends IData<K>> {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceBuilder.class);

    private final Class<V> aClass;
    private ValueConvertMapper convertMapper;
    private ICacheSource<PK, K, V> cacheSource;

    private final List<String> decorators;

    private DataSourceBuilder(Class<V> aClass, ICacheSource<PK, K, V> cacheSource) {
        this.aClass = aClass;
        this.cacheSource = cacheSource;
        this.decorators = Arrays.asList("logger");
    }

    public DataSourceBuilder<PK, K, V> setConvertMapper(ValueConvertMapper convertMapper) {
        this.convertMapper = convertMapper;
        return this;
    }

    public DataSourceBuilder<PK, K, V> setCacheSource(ICacheSource<PK, K, V> cacheSource) {
        this.cacheSource = cacheSource;
        return this;
    }

    public DataSourceBuilder<PK, K, V> addDecorator(String decorator){
        if (!decorators.contains(decorator)) {
            decorators.add(0, decorator);
        }
        return this;
    }

    public IDataSource<PK, K, V> build(){
        IDataSource<PK, K, V> dataSource = new DataSource<>(aClass, convertMapper, cacheSource);
        for (String decorator : decorators) {
            String className = DataSource.class.getName() + decorator.toUpperCase().charAt(0) + decorator.toLowerCase().substring(1);
            try {
                logger.info("init decorator:{}", className);
                Class<?> decoratorClass = Class.forName(className);
                Constructor<?> constructor = decoratorClass.getConstructor(IDataSource.class);
                constructor.setAccessible(true);
                //noinspection unchecked
                dataSource =  (IDataSource<PK, K, V>)constructor.newInstance(dataSource);
                logger.info("init decorator:{} finish", className);
            }
            catch (Throwable t) {
                logger.error("decorator error, decorator is {}", className, t);
            }
        }
        return dataSource;
    }

    public static <PK, K, V extends IData<K>> DataSourceBuilder<PK, K, V> newBuilder(Class<V> aClass, ICacheSource<PK, K, V> cacheSource){
        return new DataSourceBuilder<>(aClass, cacheSource);
    }
}
