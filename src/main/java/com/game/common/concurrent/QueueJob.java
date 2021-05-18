package com.game.common.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class QueueJob<K> implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(QueueJob.class);

	private final K queueId;
	private final String name;
	private final long nanoTimeout;

	public QueueJob(K queueId, String name) {
		this(queueId, name, 0);
	}

	public QueueJob(K queueId, String name, long nanoTimeout) {
		this.queueId = queueId;
		this.name = name;
		this.nanoTimeout = nanoTimeout;
	}

	public K getQueueId() {
		return queueId;
	}

	public String getName() {
		return name;
	}

	public long getNanoTimeout() {
		return nanoTimeout;
	}

	@Override
	public void run() {
		try {
			execute();
		}
		catch (Throwable throwable){
			logger.error("QueueJob error, {}", messageJobLog());
		}
		finally {
		}
	}

	protected abstract void execute();

	protected final String messageJobLog(){
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
				", name='" + queueJob.name + '\'' +
				", nanoTimeout=" + queueJob.nanoTimeout +
				'}';
	}
}
