package com.game.cache.source.executor;

import jodd.util.ThreadUtil;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 线程池需要优化：
 * 确保一个任务类型：name 同时只有一个线程在处理。
 * 使用单线程模式处理，存在空闲线程的时候，共享任务堵类型会堵塞name任务。
 */
public class CacheExecutor implements ICacheExecutor{

    private final ScheduledExecutorService executorService;

    public CacheExecutor(int poolSize) {
        this.executorService = Executors.newScheduledThreadPool(1);
    }

    @Override
    public <T> ICacheFuture<T> submit(CacheCallable<T> callable) {
        Future<T> future = executorService.submit(callable);
        return new CacheFuture<>(future);
    }

    @Override
    public <V> void schedule(CacheCallable<V> callable, long delay, TimeUnit unit) {
        executorService.schedule(callable, delay, unit);
    }

    @Override
    public void scheduleAtFixedRate(CacheRunnable command, long initialDelay, long period, TimeUnit unit) {
        executorService.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    private static class CacheFuture<T> implements ICacheFuture<T>{

        private volatile Future<T> future;

        public CacheFuture(Future<T> future) {
            this.future = future;
        }

        @Override
        public T get(long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            long duration = timeUnit.toMillis(timeout);
            while (duration > 0){
                if (future != null){
                    return future.get(duration, TimeUnit.MILLISECONDS);
                }
                long currentTime = System.currentTimeMillis();
                ThreadUtil.sleep(50);
                duration -= (System.currentTimeMillis() - currentTime);
                if (duration <= 0){
                    return future.get(50, TimeUnit.MILLISECONDS);
                }
            }
            throw new TimeoutException();
        }
    }
}
