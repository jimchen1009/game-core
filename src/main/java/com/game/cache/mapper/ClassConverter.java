package com.game.cache.mapper;

import com.game.cache.data.Data;
import com.game.cache.exception.CacheException;
import com.game.common.log.LogUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ClassConverter<K,V extends Data<K>> implements IClassConverter<K, V> {

    private final ClassDescription clsDescription;
    private final ValueConvertMapper mapper;

    public ClassConverter(Class<V> aClass, ValueConvertMapper convertMapper) {
        this.clsDescription = ClassDescription.get(aClass);
        this.mapper = convertMapper;
    }

    public ClassDescription getClsDescription() {
        return clsDescription;
    }

    public Class<V> getConvertedClass() {
        return (Class<V>) clsDescription.describedClass();
    }

    public V convert(Map<String, Object> cacheValues){
        Class<V> convertedClass = getConvertedClass();
        try {
            V newInstance = convertedClass.newInstance();
            for (FieldDescription description : clsDescription.fieldDescriptions()) {
                Field field = description.getField();
                ValueConverter<?> converter = mapper.getOrDefault(description.getType());
                Object object = converter.decode(cacheValues.get(description.getAnnotationName()));
                field.set(newInstance, object);
            }
            return newInstance;
        }
        catch (Throwable e) {
            throw new CacheException("cls:%s cacheValue:%s", convertedClass.getName(), LogUtil.toJSONString(cacheValues), e);
        }
    }

    public Map<String, Object> convert(V dataValue){
        try {
            Map<String, Object> cacheValue = new HashMap<>();
            for (FieldDescription description : clsDescription.getKeysDescriptions()) {
                encodeValue(dataValue, cacheValue, description, true);
            }
            for (FieldDescription description : clsDescription.getNormalDescriptions()) {
                if (dataValue.getIndexChangedBits() == 0L || dataValue.isFieldValueModified(description.getIndex())){
                    encodeValue(dataValue, cacheValue, description, false);
                }
            }
            return cacheValue;
        }
        catch (Throwable e) {
            throw new CacheException("cls:%s dataValue:%s", e, getConvertedClass().getName(), LogUtil.toJSONString(dataValue));
        }
    }

    private void encodeValue(V dataValue, Map<String, Object> cacheValue, FieldDescription description, boolean checkNullObject) throws IllegalAccessException {
        Field field = description.getField();
        ValueConverter<?> converter = mapper.getOrDefault(description.getType());
        Object encode = converter.encode(field.get(dataValue));
        if (encode != null){
            cacheValue.put(description.getAnnotationName(), encode);
        }
        else if (checkNullObject){
            throw new CacheException("name:%s can't be null", description.getAnnotationName());
        }
    }
}

