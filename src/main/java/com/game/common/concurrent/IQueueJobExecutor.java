package com.game.common.concurrent;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface IQueueJobExecutor {

	Future<?> submit(Runnable runnable);

	ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit timeUnit);

	ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit timeUnit);
}
