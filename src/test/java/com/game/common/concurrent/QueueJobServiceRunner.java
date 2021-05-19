package com.game.common.concurrent;

import com.game.common.thread.PoolThreadFactory;
import jodd.util.ThreadUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class QueueJobServiceRunner {

	private static final Logger logger = LoggerFactory.getLogger(QueueJobServiceRunner.class);

	@Test
	public void execute(){
		QueueJobService<Long> service = new QueueJobService<>(5, new PoolThreadFactory("Job"), new IQueueJobCoordinate<Long>() {
			@Override
			public Collection<? extends QueueJob> requestQueueJobs(Long queueId, int n) {
				List<MyJob> myJobList = new ArrayList<>(n);
				for (int i = 1; i <= n; i++) {
					myJobList.add(new MyJob(queueId));
				}
				return myJobList;
			}
		});
		for (int i = 0; i < 1; i++) {
			service.addQueueJob(new MyJob(i));
		}
		ThreadUtil.sleep(TimeUnit.SECONDS.toMillis(50));
		service.shutdownAsync();
	}

	private static class MyJob extends QueueJob<Long>{

		private static final Map<Long, AtomicInteger> ID_MAP = new ConcurrentHashMap<>();


		public MyJob(long queueId) {
			super(queueId, "MyJob" + ID_MAP.computeIfAbsent(queueId, key -> new AtomicInteger(0)).getAndIncrement());
		}

		@Override
		protected void execute() {
			ThreadUtil.sleep(500);
		}
	}
}
