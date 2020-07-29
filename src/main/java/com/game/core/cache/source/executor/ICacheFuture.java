package com.game.core.cache.source.executor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface ICacheFuture<T>{

    T get(long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException;
}
