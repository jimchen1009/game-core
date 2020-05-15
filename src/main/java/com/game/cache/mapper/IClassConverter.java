package com.game.cache.mapper;

import com.game.cache.data.IData;

import java.util.Map;

public interface IClassConverter<K, V extends IData<K>> {

    Class<V> getConvertedClass();

    ClassDescription getClsDescription();

    V convert(Map<String, Object> cacheValues);

    Map<String, Object> convert(V dataValue);
}
