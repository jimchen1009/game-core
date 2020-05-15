package com.game.common.lock;

import com.game.common.util.ConcurrentWeakReferenceMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LockManager {

    /**
     * key: 业务的键值，默认不会无限膨胀
     *
     */
    private static final Map<String, ConcurrentWeakReferenceMap<LockKey, ISyncLock>> name2LockMap = new ConcurrentHashMap<>();


    public static ISyncLock getSyncLock(LockKey lockKey) {
        ConcurrentWeakReferenceMap<LockKey, ISyncLock> referenceMap = getReferenceMap(lockKey);
        return referenceMap.computeIfAbsent(lockKey, SyncLock.ReentrantSyncLock::new);
    }

    private static ConcurrentWeakReferenceMap<LockKey, ISyncLock> getReferenceMap(LockKey lockKey){
        return name2LockMap.computeIfAbsent(lockKey.getPrimary(), key-> new ConcurrentWeakReferenceMap<>());
    }
}
