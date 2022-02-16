package com.game.core.cache.source.executor;

import com.game.common.concurrent.QueueJobService;
import com.game.common.thread.PoolThreadFactory;

import java.util.concurrent.TimeUnit;

/**
 * 线程池需要优化：
 * 确保一个任务类型：name 同时只有一个线程在处理。
 * 使用单线程模式处理，存在空闲线程的时候，共享任务堵类型会堵塞name任务。
 */
public class CacheExecutor implements ICacheExecutor {

    private final QueueJobService<String> jobService;

    public CacheExecutor(int poolSize) {
        this.jobService = new QueueJobService<>(poolSize, new PoolThreadFactory("Cache"));
    }

    @Override
    public <T> void submit(CacheCallable<T> callable) {
        jobService.addQueueJob(callable);
    }

    @Override
    public void scheduleWithFixedDelay(CacheRunnable command, long initialDelay, long period, TimeUnit timeUnit) {
        jobService.getExecutor().scheduleWithFixedDelay(command, initialDelay, period, timeUnit);
    }

    @Override
    public void shutdownAsync() {
        jobService.shutdownSync();
    }
}
