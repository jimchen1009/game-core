package com.game.core.cache.source.executor;

import java.util.concurrent.TimeUnit;

public interface ICacheExecutor {

    <T> void submit(CacheCallable<T> callable);

    void scheduleWithFixedDelay(CacheRunnable runnable, long initialDelay, long period, TimeUnit unit);

    void shutdownAsync();
}
