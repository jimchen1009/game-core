package com.game.cache.source.executor;

import java.util.concurrent.TimeUnit;

public interface ICacheExecutor {

    <T> ICacheFuture<T> submit(CacheCallable<T> callable);

    <V> void schedule(CacheCallable<V> callable, long delay, TimeUnit unit);

    void scheduleAtFixedRate(CacheRunnable command, long initialDelay, long period, TimeUnit unit);

    void shutdown();
}
