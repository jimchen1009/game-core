package com.game.common.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class PoolThreadFactory implements ThreadFactory {

	private final String namePrefix;
	private final AtomicInteger threadNumber;
	private final ThreadGroup threadGroup;


	public PoolThreadFactory(String namePrefix) {
		this.namePrefix = namePrefix;
		this.threadNumber = new AtomicInteger(0);
		SecurityManager securityManager = System.getSecurityManager();
		this.threadGroup = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
	}

	@Override
	public Thread newThread(Runnable runnable) {
		Thread thread = new Thread(threadGroup, runnable, namePrefix + "-" + threadNumber.getAndIncrement(), 0);
		thread.setUncaughtExceptionHandler(ThreadUncaughtExceptionHandler.INSTANCE);
		return thread;
	}
}
