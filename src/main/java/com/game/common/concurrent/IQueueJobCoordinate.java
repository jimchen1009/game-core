package com.game.common.concurrent;

import java.util.Collection;

public interface IQueueJobCoordinate<K> {

	Collection<? extends QueueJob> requestQueueJobs(K queueId, int n);
}
