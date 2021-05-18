package com.game.common.concurrent;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class QueueJobPlugExecutor implements IQueueJoExecutor{

	private final IQueueJoExecutor executor;

	public QueueJobPlugExecutor(IQueueJoExecutor executor) {
		this.executor = executor;
	}

	@Override
	public Future<?> submit(Runnable runnable) {
		return executor.submit(runnable);
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit timeUnit) {
		return executor.schedule(command, delay, timeUnit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit timeUnit) {
		return executor.scheduleWithFixedDelay(command, initialDelay, delay, timeUnit);
	}
}
