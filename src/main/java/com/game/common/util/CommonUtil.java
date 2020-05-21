package com.game.common.util;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CommonUtil {

    public static <T> T findOneIf(Collection<T> collection, Predicate<T> predicate){
        for (T data : collection) {
            if (predicate.test(data)) {
                return data;
            }
        }
        return  null;
    }

    public static <K,V,C extends Collection<V>> Map<K, C> groupByKey(Map<K, C> map, Collection<V> collection, Supplier<C> supplier, Function<V, K> function){
        for (V data : collection) {
            K key = function.apply(data);
            if (key == null){
                continue;
            }
            C container = map.computeIfAbsent(key, key0-> supplier.get());
            container.add(data);
        }
        return map;
    }
}
