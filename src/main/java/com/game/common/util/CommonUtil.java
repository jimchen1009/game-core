package com.game.common.util;

import com.google.common.base.Stopwatch;
import jodd.util.ThreadUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CommonUtil {

    public static <T> boolean findOneUtilOkayBool(Collection<T> collection, Predicate<T> predicate) {
        return findOneUtilOkay(collection, predicate) != null;
    }

    public static <T> T findOneUtilOkay(Collection<T> collection, Predicate<T> predicate) {
        for (T data : collection) {
            if (predicate.test(data)) {
                return data;
            }
        }
        return null;
    }

    public static <T> int findIndexUtilOkay(List<T> list, Predicate<T> predicate) {
        for (int index = 0; index < list.size(); index++) {
            if (predicate.test(list.get(index))) {
                return index;
            }
        }
        return -1;
    }

    public static <T> T removeOneUntilOkay(List<T> dataList, Predicate<T> predicate) {
        for (int i = 0; i < dataList.size(); i++) {
            T data = dataList.get(i);
            if (predicate.test(data)) {
                dataList.remove(i);
                return data;
            }
        }
        return null;
    }

    public static <T, V> V applyOneUtilOkay(Collection<T> collection, Function<T, V> function) {
        for (T data : collection) {
            V apply = function.apply(data);
            if (apply != null) {
                return apply;
            }
        }
        return null;
    }

    /**
     * splitUp1Into1Group
     *
     * @param map
     * @param collection
     * @param supplier
     * @param function
     * @param <K>
     * @param <V>
     * @param <C>
     * @return
     */
    public static <K, V, C extends Collection<V>> Map<K, C> splitUp1Into1Group(Map<K, C> map, Collection<V> collection, Supplier<C> supplier, Function<V, K> function) {
        for (V data : collection) {
            K key = function.apply(data);
            if (key == null) {
                continue;
            }
            C container = map.computeIfAbsent(key, key0 -> supplier.get());
            container.add(data);
        }
        return map;
    }

    public static <K, V, C extends Collection<V>> Map<K, C> splitUpIntoNGroup(Map<K, C> map, Collection<V> collection, Supplier<C> supplier, Function<V, Collection<K>> function) {
        for (V data : collection) {
            Collection<K> keys = function.apply(data);
            if (keys == null || keys.isEmpty()) {
                continue;
            }
            for (K key : keys) {
                C container = map.get(key);
                if (container == null) {
                    container = supplier.get();
                    map.put(key, container);
                }
                container.add(data);
            }
        }
        return map;
    }

    public static <K, V> Map<K, V> stream2Map(Collection<V> collection, Function<V, K> function) {
        return collection.stream().collect(Collectors.toMap(function, data -> data));
    }


    public static void whileLoopUntilOkay(final long timeout, Supplier<Boolean> supplier) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (!SafeSupplier.class.isAssignableFrom(supplier.getClass())) {
            supplier = new SafeSupplier<>(supplier, false);
        }
        while (!supplier.get() && (timeout == 0 || stopwatch.elapsed(TimeUnit.MILLISECONDS) < timeout)){
            ThreadUtil.sleep(25);   //
        }
    }
}
