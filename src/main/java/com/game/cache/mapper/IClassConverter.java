package com.game.cache.mapper;

import com.game.cache.data.IData;

import java.util.Map;

public interface IClassConverter<K, V extends IData<K>> {

    Class<V> getConvertedClass();

    V convert(Map<String, Object> cacheValue);

    Map<String, Object> convert(V dataValue);
}
