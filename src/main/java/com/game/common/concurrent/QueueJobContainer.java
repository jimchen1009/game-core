package com.game.common.concurrent;

import com.game.common.util.CommonUtil;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class QueueJobContainer<K> {

	private static final long SCHEDULE_TIME = TimeUnit.SECONDS.toMillis(1);

	private static final Logger logger = LoggerFactory.getLogger(QueueJobContainer.class);

	private final K queueId;
	private final AtomicLong atomicLong;
	private final IQueueJobExecutor executor;
	private final IQueueJobCoordinate<K> coordinate;
	private final ConcurrentLinkedQueue<QueueInnerJob> innerJobQueue;
	private final AtomicBoolean runningBool;
	private volatile QueueInnerJob runningInnerJob;	//当前正在执行的JOB
	private final AtomicBoolean runningLock;

	private final ScheduledFuture<?> scheduledFuture;

	public QueueJobContainer(K queueId, IQueueJobExecutor executor, IQueueJobCoordinate<K> coordinate) {
		this.queueId = queueId;
		this.atomicLong = new AtomicLong(0);
		this.executor = new QueueJobPlugExecutor(executor);
		this.coordinate = coordinate;
		this.runningBool = new AtomicBoolean(true);
		this.innerJobQueue = new ConcurrentLinkedQueue<>();
		this.runningInnerJob = null;
		this.runningLock = new AtomicBoolean(false);
		this.scheduledFuture = this.executor.scheduleWithFixedDelay(this::onScheduleAll, SCHEDULE_TIME, SCHEDULE_TIME, TimeUnit.MILLISECONDS);
	}

	public boolean addQueueJob(QueueJob queueJob){
		if (queueJob.getQueueId() != this.queueId){
			throw new UnsupportedOperationException(queueJob.toJobLog());
		}
		if (!runningBool.get()) {
			logger.error("shutdownSync, queueJob: {}", queueJob.toJobLog());
			return false;
		}
		innerJobQueue.add(new QueueInnerJob(atomicLong.incrementAndGet(), queueJob, this::finishQueueJob));
		lockCheckRunningJobAndPollQueue(Objects::isNull);
		return true;
	}

	public void shutdownSync(){
		if (!runningBool.compareAndSet(true, false)) {
			return;
		}
		CommonUtil.whileLoopUntilOkay(TimeUnit.SECONDS.toMillis(30), innerJobQueue::isEmpty);
		CommonUtil.whileLoopUntilOkay(TimeUnit.SECONDS.toMillis(30), this::currentIsIdle);
		scheduledFuture.cancel(true);
	}

	private void onScheduleAll(){
		try {
			LockCode timeoutCode = lockCheckRunningJobAndPollQueue(innerJob -> innerJob != null && innerJob.timeoutBool());
			if (timeoutCode.isSuccess()) {
				timeoutCode.currentInnerJob.timeoutCancel();
				logger.error("cancel timeout queueJob: {}", timeoutCode.currentInnerJob.queueJob.toJobLog());
			}
			if (currentIsIdle()) {
				requestQueueJobs();
			}
		}
		catch (Throwable throwable){
			logger.error("{}", throwable);
		}
	}

	/**
	 * @param predicate
	 * @return
	 */
	private LockCode lockCheckRunningJobAndPollQueue(Predicate<QueueInnerJob> predicate){
		LockCode lockCode = LockCode.failureCode();
		synchronized (runningLock){
			if (predicate.test(runningInnerJob)){
				QueueInnerJob nextQueueJob = innerJobQueue.poll();
				QueueInnerJob currentInnerJob = runningInnerJob;
				runningInnerJob = nextQueueJob;
				lockCode = LockCode.successCode(nextQueueJob, currentInnerJob);
			}
		}
		if (lockCode.isSuccess() ){
			if (lockCode.nextQueueJob != null){
				try {
					Future<?> future = executor.submit(lockCode.nextQueueJob);
					lockCode.nextQueueJob.submitJob(future);
				}
				catch (Throwable throwable){
					logger.error("{}", throwable);
				}
			}
			if (lockCode.currentInnerJob != null && lockCode.nextQueueJob == null){
				requestQueueJobs();
			}
		}
		return lockCode;
	}

	/**
	 * 请求任务
	 */
	private void requestQueueJobs(){
		if (!runningBool.get()) {
			return;
		}
		Collection<? extends QueueJob> queueJobs = coordinate.requestQueueJobs(queueId, 5);
		if (!queueJobs.isEmpty()) {
			queueJobs.forEach( queueJob -> innerJobQueue.add(createInnerJob(queueJob)));
			logger.debug("任务请求, 长度: {}", queueJobs.size());
			lockCheckRunningJobAndPollQueue(Objects::isNull);
		}
	}

	/**
	 * 创建内部任务
	 * @param queueJob
	 * @return
	 */
	private QueueInnerJob createInnerJob(QueueJob queueJob){
		return new QueueInnerJob(atomicLong.incrementAndGet(), queueJob, this::finishQueueJob);
	}

	/**
	 * 现在是不是空闲状态
	 * @return
	 */
	private boolean currentIsIdle(){
		LockCode idleCode = lockCheckRunningJobAndPollQueue(Objects::isNull);
		return idleCode.isSuccess() && idleCode.nextQueueJob == null;
	}

	private void finishQueueJob(QueueInnerJob queueInnerJob){
		Objects.requireNonNull(queueInnerJob);
		LockCode lockCode = lockCheckRunningJobAndPollQueue(innerJob -> innerJob != null && innerJob.getInnerUniqueId() == queueInnerJob.getInnerUniqueId());
		if (lockCode.isSuccess()){
			lockCode.currentInnerJob.completeInAdvance();
		}
	}

	private static class QueueInnerJob extends FutureTask<Object>{

		private final long innerUniqueId;
		private final QueueJob queueJob;
		private final Consumer<QueueInnerJob> consumer;
		private volatile Stopwatch stopwatch;
		private volatile Future<?> future;

		public QueueInnerJob(long innerUniqueId, QueueJob<?> queueJob, Consumer<QueueInnerJob> consumer) {
			super(Executors.callable(queueJob));
			this.innerUniqueId = innerUniqueId;
			this.queueJob = queueJob;
			this.consumer = consumer;
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
			logger.debug("取消任务: {}", queueJob.toJobLog());
		}

		public void completeInAdvance(){
			logger.debug("任务完成: {}", queueJob.toJobLog());
		}

		@Override
		protected void done() {
			super.done();
			consumer.accept(this);
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
