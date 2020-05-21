package com.game.common.util;

import java.util.Collection;
import java.util.function.Predicate;

public class CommonUtil {

    public static <T> T findOneIf(Collection<T> collection, Predicate<T> predicate){
        for (T data : collection) {
            if (predicate.test(data)) {
                return data;
            }
        }
        return  null;
    }
}
