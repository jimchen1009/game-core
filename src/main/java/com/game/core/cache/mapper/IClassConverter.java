package com.game.core.cache.mapper;

import com.game.core.cache.data.IData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public interface IClassConverter<K, V extends IData<K>> {

    Class<V> getConvertedClass();

    default V convert2Data(Map<String, Object> dataValueMap){
        return convert2Data(dataValueMap::get);
    }

    V convert2Data(Function function);

    default Map<String, Object> convert2Cache(V data){
        Map<String, Object> cacheValue = new HashMap<>();
        convert2Cache(data, cacheValue::put);
        return cacheValue;
    }

    void convert2Cache(V dataValue, BiConsumer<String, Object> consumer);


    interface Function{

        Object apply(String t) throws Exception;
    }
}
