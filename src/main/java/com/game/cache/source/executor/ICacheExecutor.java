package com.game.cache.source.executor;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface ICacheExecutor {

    <T> Future<T> submit(CacheCallable<T> callable);

    ScheduledFuture<?> scheduleAtFixedRate(CacheRunnable command, long initialDelay, long period, TimeUnit unit);

    void shutdown();
}
