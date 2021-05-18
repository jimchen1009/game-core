package com.game.common.concurrent;

import com.game.common.thread.PoolThreadFactory;
import jodd.util.ThreadUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class QueueJobServiceRunner {

	private static final Logger logger = LoggerFactory.getLogger(QueueJobServiceRunner.class);

	@Test
	public void execute(){
		QueueJobService service = new QueueJobService(5, new PoolThreadFactory("Job"));
		for (int i = 0; i < 1000; i++) {
			service.addQueueJob(new MyJob(0, i));
			service.addQueueJob(new MyJob(1, i));
			service.addQueueJob(new MyJob(2, i));
			service.addQueueJob(new MyJob(3, i));
		}
		service.shutdownGracefully();
	}

	private static class MyJob extends QueueJob{

		private static final Map<Long, AtomicInteger> INTEGER_MAP = new ConcurrentHashMap<>();

		private final int id;

		public MyJob(long queueId, int id) {
			super(queueId, "MyJob" + id);
			this.id = id;
		}

		@Override
		protected void execute() {
			AtomicInteger atomicInteger = INTEGER_MAP.computeIfAbsent(getQueueId(), key -> new AtomicInteger(0));
			if (atomicInteger.getAndIncrement() == id) {
				logger.debug("成功:{}, id:{}", getQueueId(), id);
			}
			else {
				logger.error("失败:{}, id:{}", getQueueId(), id);
			}
			ThreadUtil.sleep(10);
		}
	}
}
