package com.game.common.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class QueueJobCoreExecutor implements IQueueJobExecutor {

	private final ScheduledExecutorService executorService;

	public QueueJobCoreExecutor(int corePoolSize, ThreadFactory threadFactory) {
		this.executorService = Executors.newScheduledThreadPool(corePoolSize, threadFactory);
	}

	public void shutdown(){
		executorService.shutdown();
	}

	@Override
	public Future<?> submit(Runnable runnable) {
		return executorService.submit(runnable);
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit timeUnit) {
		return executorService.schedule(command, delay, timeUnit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit timeUnit) {
		return executorService.scheduleWithFixedDelay(command, initialDelay, delay, timeUnit);
	}
}
