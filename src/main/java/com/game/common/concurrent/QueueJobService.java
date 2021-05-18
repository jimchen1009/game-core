package com.game.common.concurrent;

import jodd.util.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class QueueJobService<K> {

	private static final Logger logger = LoggerFactory.getLogger(QueueJobService.class);

	private final Map<K, QueueJobContainer<K>> containerMap;
	private final QueueJobCoreExecutor executor;
	private final AtomicBoolean runningBool;

	public QueueJobService(int corePoolSize, ThreadFactory threadFactory) {
		this.containerMap = new ConcurrentHashMap<>();
		this.executor = new QueueJobCoreExecutor(corePoolSize, threadFactory);
		this.runningBool = new AtomicBoolean(true);
		/** 统一调度, 避免产生多个任务 1. 暂时单线程检测 */
		this.executor.scheduleWithFixedDelay(this::onScheduleAll, 1, 1, TimeUnit.SECONDS);
	}

	public void addQueueJob(QueueJob<K> queueJob){
		if (runningBool.get()){
			getJobContainer(queueJob).addQueueJob(queueJob);
		}
		else {
			logger.error("Service's shutdown, queueJob={}", queueJob.messageJobLog());
		}
	}
	public void shutdownGracefully(){
		if (!runningBool.compareAndSet(true, false)) {
			return;
		}
		while (existRunningJobContainer()){
			ThreadUtil.sleep(TimeUnit.MILLISECONDS.toMillis(25));
		}
		executor.shutdown();
	}

	private QueueJobContainer getJobContainer(QueueJob<K> queueJob){
		return containerMap.computeIfAbsent(queueJob.getQueueId(), id-> new QueueJobContainer<>(id, executor));
	}

	private boolean existRunningJobContainer(){
		return containerMap.values().stream().anyMatch(container -> !container.queueIsEmpty());
	}

	private void onScheduleAll(){
		for (Map.Entry<K, QueueJobContainer<K>> entry : containerMap.entrySet()) {
			entry.getValue().onScheduleAll();
		}
	}
}
