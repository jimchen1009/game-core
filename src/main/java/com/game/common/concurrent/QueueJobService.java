package com.game.common.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public class QueueJobService<K> {

	private static final Logger logger = LoggerFactory.getLogger(QueueJobService.class);

	private final Map<K, QueueJobContainer<K>> containerMap;
	private final QueueJobCoreExecutor executor;
	private final IQueueJobCoordinate<K> coordinate;
	private final AtomicBoolean runningBool;

	public QueueJobService(int corePoolSize, ThreadFactory threadFactory) {
		this(corePoolSize, threadFactory, new QueueJobCoordinate<>());
	}

	public QueueJobService(int corePoolSize, ThreadFactory threadFactory, IQueueJobCoordinate<K> coordinate) {
		this.containerMap = new ConcurrentHashMap<>();
		this.executor = new QueueJobCoreExecutor(corePoolSize, threadFactory);
		this.coordinate = coordinate;
		this.runningBool = new AtomicBoolean(true);
	}

	public void addQueueJob(QueueJob<K> queueJob){
		if (runningBool.get()){
			getJobContainer(queueJob).addQueueJob(queueJob);
		}
		else {
			logger.error("Service's shutdownAsync, queueJob: {}", queueJob.messageJobLog());
		}
	}
	public void shutdownAsync(){
		if (!runningBool.compareAndSet(true, false)) {
			return;
		}
		for (QueueJobContainer<K> container : containerMap.values()) {
			container.shutdownAsync();
		}
		executor.shutdown();
	}

	private QueueJobContainer getJobContainer(QueueJob<K> queueJob){
		return containerMap.computeIfAbsent(queueJob.getQueueId(), id-> new QueueJobContainer<>(id, executor, coordinate));
	}
}
