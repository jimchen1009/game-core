package com.game.cache.mapper;

import com.game.cache.CacheType;
import com.game.cache.data.IData;
import com.game.cache.exception.CacheException;
import com.game.common.log.LogUtil;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public IClassAnnotation getClassAnnotation() {
        return classAnnotation;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<V> getConvertedClass() {
        return aClass;
    }

    @Override
    public V convert2Value(Map<String, Object> cacheValue){
        Class<V> convertedClass = getConvertedClass();
        try {
            V newInstance = convertedClass.newInstance();
            for (FieldAnnotation description : classAnnotation.getFiledAnnotationList()) {
                Field field = description.getField();
                ValueConverter<?> converter = cacheType.getConvertMapper().getOrDefault(description.getType());
                Object object = converter.decode(cacheValue.get(description.getAnnotationName()));
                field.set(newInstance, object);
            }
            return newInstance;
        }
        catch (Throwable e) {
            throw new CacheException("cls:%s cacheValue:%s", e, convertedClass.getName(), LogUtil.toJSONString(cacheValue));
        }
    }

    @Override
    public List<V> convert2ValueList(Collection<Map<String, Object>> cacheValues) {
        return cacheValues.stream().map(this::convert2Value).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> convert2Cache(V dataValue){
        try {
            Map<String, Object> cacheValue = new HashMap<>();
            for (FieldAnnotation description : classAnnotation.getPrimaryFieldAnnotationList()) {
                encodeValue(dataValue, cacheValue, description, true);
            }
            boolean cacheBitIndex = !dataValue.existCacheBitIndex();
            for (FieldAnnotation description : classAnnotation.getNormalFieldAnnotationList()) {
                encodeValue(dataValue, cacheValue, description, true);
                if (cacheBitIndex || cacheType.isFullCache() || dataValue.hasBitIndex(description.getBitIndex())){
                    encodeValue(dataValue, cacheValue, description, false);
                }
            }
            return cacheValue;
        }
        catch (Throwable e) {
            throw new CacheException("cls:%s dataValue:%s", e, getConvertedClass().getName(), LogUtil.toJSONString(dataValue));
        }
    }

    @Override
    public List<Map<String, Object>> convert2CacheList(Collection<V> dataValues) {
        return dataValues.stream().map(this::convert2Cache).collect(Collectors.toList());
    }

    private void encodeValue(V dataValue, Map<String, Object> cacheValue, FieldAnnotation description, boolean checkNullObject) throws IllegalAccessException {
        Field field = description.getField();
        ValueConverter<?> converter = cacheType.getConvertMapper().getOrDefault(description.getType());
        Object encode = converter.encode(field.get(dataValue));
        if (encode != null){
            cacheValue.put(description.getAnnotationName(), encode);
        }
        else if (checkNullObject){
            throw new CacheException("name:%s can't be null", description.getAnnotationName());
        }
    }
}

