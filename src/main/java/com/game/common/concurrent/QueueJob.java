package com.game.common.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class QueueJob<K> implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(QueueJob.class);

	private final K queueId;
	private final String message;
	private volatile long nanoTimeout;

	public QueueJob(K queueId, String message) {
		this.queueId = queueId;
		this.message = message;
	}

	public K getQueueId() {
		return queueId;
	}

	public String getMessage() {
		return message;
	}

	public long getNanoTimeout() {
		return nanoTimeout;
	}

	public QueueJob<K> setNanoTimeout(long nanoTimeout) {
		this.nanoTimeout = nanoTimeout;
		return this;
	}

	@Override
	public void run() {
		try {
			execute();
		}
		catch (Throwable throwable){
			logger.error("queueJob error, {}", toJobLog());
		}
		finally {
		}
	}

	protected abstract void execute();

	protected final String toJobLog(){
		try {
			return this.toString();
		}
		catch (Throwable throwable){
			return toString(this);
		}
	}

	@Override
	public String toString() {
		return toString(this);
	}

	private static String toString(QueueJob<?> queueJob) {
		return "{" +
				"queueId=" + queueJob.queueId +
				", message='" + queueJob.message + '\'' +
				", nanoTimeout=" + queueJob.nanoTimeout +
				'}';
	}
}
