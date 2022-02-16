package com.game.core.cache.mapper;

import com.game.common.log.LogUtil;
import com.game.core.cache.CacheType;
import com.game.core.cache.data.IData;
import com.game.core.cache.exception.CacheException;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.BiConsumer;

public class ClassConverter<K,V extends IData<K>> implements IClassConverter<K, V> {

    private final Class<V> aClass;
    private final IClassAnnotation classAnnotation;
    private final CacheType cacheType;

    public ClassConverter(Class<V> aClass, IClassAnnotation classAnnotation, CacheType cacheType) {
        this.aClass = aClass;
        this.classAnnotation = classAnnotation;
        this.cacheType = cacheType;
    }


    @Override
    public Class<V> getConvertedClass() {
        return aClass;
    }

    @Override
    public V convert2Data(Map<String, Object> dataValueMap){
        return convert2Data(dataValueMap::get);
    }

    @Override
    public V convert2Data(Function function) {
        Class<V> convertedClass = getConvertedClass();
        try {
            V newInstance = convertedClass.newInstance();
            for (FieldAnnotation description : classAnnotation.getFiledAnnotationList()) {
                Field field = description.getField();
                ValueConverter<?> converter = cacheType.getConvertMapper().getOrDefault(description.getType());
                Object object = converter.decode(function.apply(description.getName()));
                field.set(newInstance, object);
            }
            return newInstance;
        }
        catch (Throwable e) {
            throw new CacheException("cacheType:%s, convertedClass:%s", e, cacheType.name(), convertedClass.getName());
        }
    }

    @Override
    public void convert2Cache(V dataValue, BiConsumer<String, Object> consumer) {
        try {
            for (FieldAnnotation description : classAnnotation.getFiledAnnotationList()) {
                Field field = description.getField();
                ValueConverter<?> converter = cacheType.getConvertMapper().getOrDefault(description.getType());
                Object encode = converter.encode(field.get(dataValue));
                consumer.accept(description.getName(), encode);
            }
        }
        catch (Throwable e) {
            throw new CacheException("cacheType:%s, convertedClass:%s, dataValue:%s", e, cacheType.name(),  getConvertedClass().getName(), LogUtil.toJSONString(dataValue));
        }
    }
}

