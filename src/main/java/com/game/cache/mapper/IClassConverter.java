package com.game.cache.mapper;

import com.game.cache.data.IData;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IClassConverter<K, V extends IData<K>> {

    Class<V> getConvertedClass();

    ClassDescription getClsDescription();

    V convert2Value(Map<String, Object> cacheValue);

    List<V> convert2ValueList(Collection<Map<String, Object>> cacheValues);

    Map<String, Object> convert2Cache(V dataValue);

    List<Map<String, Object>> convert2CacheList(Collection<V> dataValues);
}
