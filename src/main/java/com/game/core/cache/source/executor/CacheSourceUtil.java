package com.game.core.cache.source.executor;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class CacheSourceUtil{

    public static void submitCallable(ICacheSource cacheSource, String message, Callable<Boolean> callable, Consumer<Boolean> consumer){
        String name = cacheSource.getCacheUniqueId().getName();
        CacheCallable<Boolean> cacheCallable = new CacheCallable<>(name, message, callable, consumer);
        cacheSource.getExecutor().submit(cacheCallable);
    }
}
