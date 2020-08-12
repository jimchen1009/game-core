package com.game.common.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CommonUtil {

    public static <T> T findOneIf(Collection<T> collection, Predicate<T> predicate){
        for (T data : collection) {
            if (predicate.test(data)) {
                return data;
            }
        }
        return  null;
    }

    public static <T> T removeOneIf(List<T> dataList, Predicate<T> predicate){
        for (int i = 0; i < dataList.size(); i++) {
            T data = dataList.get(i);
            if (predicate.test(data)) {
                dataList.remove(i);
                return data;
            }
        }
        return  null;
    }

    public static <T, V> V applyOneIf(Collection<T> collection, Function<T, V> function){
        for (T data : collection) {
            V apply = function.apply(data);
            if (apply != null){
                return apply;
            }
        }
        return  null;
    }

    public static <K,V,C extends Collection<V>> Map<K, C> group2Collection(Map<K, C> map, Collection<V> collection, Supplier<C> supplier, Function<V, K> function){
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

    public static <K,V> Map<K, V> stream2Map( Collection<V> collection, Function<V, K> function){
        return collection.stream().collect(Collectors.toMap(function, data-> data));
    }
}
