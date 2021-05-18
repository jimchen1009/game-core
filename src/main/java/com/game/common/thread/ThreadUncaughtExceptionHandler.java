package com.game.common.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(ThreadUncaughtExceptionHandler.class);

	public static ThreadUncaughtExceptionHandler INSTANCE = new ThreadUncaughtExceptionHandler();

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		logger.error("uncaught exception of {}/{}", t.getId(), t.getName(), e);
	}
}
