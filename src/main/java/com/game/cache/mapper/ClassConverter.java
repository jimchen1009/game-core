package com.game.cache.mapper;

import com.game.cache.CacheName;
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

    private final ClassInformation information;
    private final CacheType cacheType;

    public ClassConverter(Class<V> aClass, CacheType cacheType) {
        this.information = ClassInformation.get(aClass);
        this.cacheType = cacheType;
    }

    public ClassInformation getInformation() {
        return information;
    }

    @SuppressWarnings("unchecked")
    public Class<V> getConvertedClass() {
        return (Class<V>) information.getAClass();
    }

    public V convert2Value(Map<String, Object> cacheValue){
        Class<V> convertedClass = getConvertedClass();
        try {
            V newInstance = convertedClass.newInstance();
            for (FieldInformation description : information.fieldDescriptionList()) {
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

    public Map<String, Object> convert2Cache(V dataValue){
        try {
            Map<String, Object> cacheValue = new HashMap<>();
            for (FieldInformation description : information.getKeysDescriptions()) {
                encodeValue(dataValue, cacheValue, description, true);
            }
            for (FieldInformation description : information.getNormalDescriptions()) {
                if (description.isInternal()){
                    if (cacheType.isCacheInternal()){
                        encodeValue(dataValue, cacheValue, description, true);
                    }
                }
                if (cacheType.isFullCache() || dataValue.hasBitIndex(description.getBitIndex())){
                    encodeValue(dataValue, cacheValue, description, false);
                }
            }
            if (cacheType.isFullCache()){
                ValueConverter<?> converter = cacheType.getConvertMapper().get(int.class);
                Object encode = converter.encode(dataValue.getBitIndexBits());
                cacheValue.put(CacheName.DATA_BITS.getKeyName(), encode);
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

    private void encodeValue(V dataValue, Map<String, Object> cacheValue, FieldInformation description, boolean checkNullObject) throws IllegalAccessException {
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

