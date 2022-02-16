package com.game.core.cache.mapper;

import java.lang.reflect.ParameterizedType;

public abstract class ValueConverter<T> {

    private final T defaultDataValue;
    private final Object defaultCacheValue;
    private final Class<T> aClass;

    /**
     *
     * @param defaultDataValue 内存值的默认值
     * @param defaultCacheValue 缓存的默认值
     */
    @SuppressWarnings("unchecked")
    public ValueConverter(T defaultDataValue, Object defaultCacheValue) {
        this.defaultDataValue = defaultDataValue;
        this.defaultCacheValue = defaultCacheValue;
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        this.aClass = (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }

    /**
     * 获取泛型类型
     * @return
     */
    public Class<T> getGenericClass() {
        return aClass;
    }

    public final T decode(Object cacheValue){
        if (cacheValue == null || cacheValue.equals(defaultCacheValue)){
            return defaultDataValue;
        }
        return decode0(cacheValue);
    }

    /**
     * 数据源 -> 缓存数据
     * @param cacheValue
     * @return
     */
    protected abstract T decode0(Object cacheValue);


    public Object encode(Object dataValue){
        if (dataValue == null || dataValue.equals(defaultDataValue)){
            return defaultCacheValue;
        }
        return encode0(dataValue);
    }

    /**
     * 内存数据 -> 数据源
     * @param dataValue
     * @return
     */
    protected abstract Object encode0(Object dataValue);
}
