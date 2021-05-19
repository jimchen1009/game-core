package com.game.common.concurrent;

import java.util.Collection;
import java.util.Collections;

public class QueueJobCoordinate<K> implements IQueueJobCoordinate<K> {

	@Override
	public Collection<? extends QueueJob> requestQueueJobs(K queueId, int n) {
		return Collections.emptyList();
	}
}
