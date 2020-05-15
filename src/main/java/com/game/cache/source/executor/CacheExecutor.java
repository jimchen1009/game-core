package com.game.cache.source.executor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheExecutor implements ICacheExecutor{

    private final AtomicInteger atomicInteger;
    private final ScheduledExecutorService[] executorServices;
    private final Map<String, ScheduledExecutorService> executorServiceMap;

    public CacheExecutor(int poolSize) {
        this.atomicInteger = new AtomicInteger(0);
        this.executorServices = new ScheduledExecutorService[poolSize];
        for (int i = 0; i < poolSize; i++) {
            this.executorServices[i]  = Executors.newScheduledThreadPool(poolSize);
        }
        this.executorServiceMap = new ConcurrentHashMap<>();
    }

    @Override
    public <T> Future<T> submit(CacheCallable<T> callable) {
        getExecutorService(callable.getName()).shutdown();
        return getExecutorService(callable.getName()).submit(callable);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(CacheRunnable command, long initialDelay, long period, TimeUnit unit) {
        return getExecutorService(command.getName()).scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public void shutdown() {
        for (ScheduledExecutorService executorService : executorServices) {
            executorService.shutdown();
        }
    }

    private ScheduledExecutorService getExecutorService(String name){
        return executorServiceMap.computeIfAbsent(name, key->{
            int andIncrement = atomicInteger.getAndIncrement();
            int index = andIncrement % executorServices.length;
            return executorServices[index];
        });
    }
}
