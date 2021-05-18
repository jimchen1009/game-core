package com.game.common.concurrent;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class QueueJobContainer {

	private static final Logger logger = LoggerFactory.getLogger(QueueJobContainer.class);

	private final long queueId;
	private final AtomicLong atomicLong;
	private final IQueueJoExecutor executor;
	private final ConcurrentLinkedQueue<QueueInnerJob> innerJobQueue;
	private volatile QueueInnerJob runningInnerJob;	//当前正在执行的JOB
	private final AtomicBoolean runningLock;

	public QueueJobContainer(long queueId, IQueueJoExecutor executor) {
		this.queueId = queueId;
		this.atomicLong = new AtomicLong(0);
		this.executor = new QueueJobPlugExecutor(executor);
		this.innerJobQueue = new ConcurrentLinkedQueue<>();
		this.runningInnerJob = null;
		this.runningLock = new AtomicBoolean(false);

		this.executor.scheduleWithFixedDelay(this::onScheduleAll, 1, 1, TimeUnit.SECONDS);
	}

	public void addQueueJob(QueueJob queueJob){
		if (queueJob.getQueueId() != this.queueId){
			throw new UnsupportedOperationException("");
		}
		innerJobQueue.add(new QueueInnerJob(queueJob));
		lockCheckRunningJobAndPollQueue(Objects::isNull, true);
	}

	public boolean queueIsEmpty(){
		return innerJobQueue.isEmpty();
	}

	public void onScheduleAll(){
		try {
			LockCode lockCode = lockCheckRunningJobAndPollQueue(innerJob -> innerJob != null && innerJob.timeoutBool(), false);
			if (lockCode.isSuccess()) {
				lockCode.currentInnerJob.timeoutCancel();
				logger.error("cancel timeout queueJob={}", lockCode.currentInnerJob.queueJob.messageJobLog());
			}
		}
		catch (Throwable throwable){
			logger.error("{}", throwable);
		}
	}

	/**
	 * @param predicate
	 * @param supportConcurrency 强制加锁并发
	 * @return
	 */
	private LockCode lockCheckRunningJobAndPollQueue(Predicate<QueueInnerJob> predicate, boolean supportConcurrency){
		if (!supportConcurrency && !predicate.test(runningInnerJob)){
			return LockCode.failureCode();
		}
		LockCode lockCode = LockCode.failureCode();
		synchronized (runningLock){
			if (predicate.test(runningInnerJob)){
				QueueInnerJob nextQueueJob = innerJobQueue.poll();
				QueueInnerJob currentInnerJob = runningInnerJob;
				runningInnerJob = nextQueueJob;
				lockCode = LockCode.successCode(nextQueueJob, currentInnerJob);
			}
		}
		if (lockCode.isSuccess() && lockCode.nextQueueJob != null){
			try {
				Future<?> future = executor.submit(lockCode.nextQueueJob);
				lockCode.nextQueueJob.submitJob(future);
			}
			catch (Throwable throwable){
				logger.error("{}", throwable);
			}
		}
		return lockCode;
	}

	private void finishQueueJob(QueueInnerJob queueInnerJob){
		Objects.requireNonNull(queueInnerJob);
		LockCode lockCode = lockCheckRunningJobAndPollQueue(innerJob -> innerJob != null && innerJob.getInnerUniqueId() == queueInnerJob.getInnerUniqueId(), true);
		if (lockCode.isSuccess()){
			lockCode.currentInnerJob.completeInAdvance();
		}
		else {
			logger.error("{}", queueInnerJob.queueJob.messageJobLog());
		}
	}

	private class QueueInnerJob extends FutureTask<Object>{

		private final long innerUniqueId;
		private final QueueJob queueJob;
		private volatile Stopwatch stopwatch;
		private volatile Future<?> future;

		public QueueInnerJob(QueueJob queueJob) {
			super(Executors.callable(queueJob));
			this.innerUniqueId = atomicLong.incrementAndGet();
			this.queueJob = queueJob;
		}

		public long getInnerUniqueId() {
			return innerUniqueId;
		}

		public void submitJob(Future<?> future){
			this.future = future;
			this.stopwatch = Stopwatch.createStarted();
		}

		public boolean timeoutBool(){
			if (stopwatch == null || future == null || queueJob.getNanoTimeout() == 0) {
				return false;
			}
			return stopwatch.elapsed(TimeUnit.NANOSECONDS) >= queueJob.getNanoTimeout();
		}

		public void timeoutCancel(){
			future.cancel(true);
//			logger.trace("取消任务: {}", queueJob.messageJobLog());
		}

		public void completeInAdvance(){
//			logger.trace("任务完成: {}", queueJob.messageJobLog());
		}

		@Override
		protected void done() {
			super.done();
			finishQueueJob(this);
		}
	}


	private static class LockCode{

		private final boolean isSuccess;
		private QueueInnerJob nextQueueJob;
		private QueueInnerJob currentInnerJob;

		public LockCode(boolean isSuccess, QueueInnerJob nextQueueJob, QueueInnerJob currentInnerJob) {
			this.isSuccess = isSuccess;
			this.nextQueueJob = nextQueueJob;
			this.currentInnerJob = currentInnerJob;
		}

		public boolean isSuccess() {
			return isSuccess;
		}

		public static LockCode failureCode(){
			return new LockCode(false, null, null);
		}

		public static LockCode successCode(QueueInnerJob nextQueueJob, QueueInnerJob currentInnerJob){
			return new LockCode(true, nextQueueJob, currentInnerJob);
		}
	}
}
