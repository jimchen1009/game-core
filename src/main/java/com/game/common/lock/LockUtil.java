package com.game.common.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 *
 * [19:17:29:194] [ERROR] [pool-2-thread-9] com.game.common.lock.LockUtil.unlockAll(LockUtil.java:121): lock:{primary='@cache', secondary='UserItem.17'} unlock success.
 * java.lang.IllegalMonitorStateException
 * 	at java.util.concurrent.locks.ReentrantLock$Sync.tryRelease(ReentrantLock.java:151) ~[?:1.8.0_45]
 * 	at java.util.concurrent.locks.AbstractQueuedSynchronizer.release(AbstractQueuedSynchronizer.java:1261) ~[?:1.8.0_45]
 * 	at java.util.concurrent.locks.ReentrantLock.unlock(ReentrantLock.java:457) ~[?:1.8.0_45]
 * 	at com.game.common.lock.SyncLock$ReentrantSyncLock.unlock(SyncLock.java:58) ~[classes/:?]
 * 	at com.game.common.lock.LockUtil.unlockAll(LockUtil.java:108) [classes/:?]
 * 	at com.game.common.lock.LockUtil.unlockAll(LockUtil.java:102) [classes/:?]
 * 	at com.game.common.lock.LockUtil.syncLock(LockUtil.java:66) [classes/:?]
 * 	at com.game.common.lock.LockUtil.syncLock(LockUtil.java:37) [classes/:?]
 * 	at com.game.cache.data.PrimaryDataContainer.replaceBatch(PrimaryDataContainer.java:77) [classes/:?]
 * 	at com.game.cache.data.DataContainer.replaceBatch(DataContainer.java:77) [classes/:?]
 * 	at com.game.cache.dao.DataCacheMapDao.replaceBatch(DataCacheMapDao.java:70) [classes/:?]
 * 	at com.game.cache.source.CacheRunner.lambda$0(CacheRunner.java:97) [classes/:?]
 * 	at com.game.cache.source.CacheRunner$$Lambda$43/154827180
 *
 *
 */
public class LockUtil {

    private static final long MILLISECONDS = 500L;
    private static final long WARN_TIME = 500L;

    private static final Logger logger = LoggerFactory.getLogger(LockUtil.class);

    public static boolean syncLock(LockKey lockKey, String message, Runnable runnable){
        return syncLock(Collections.singleton(lockKey), MILLISECONDS, message, runnable);
    }

    public static boolean syncLock(Collection<LockKey> lockKeys, String message, Runnable runnable){
        return syncLock(lockKeys, MILLISECONDS, message, runnable);
    }

    public static boolean syncLock(Collection<LockKey> lockKeys, long milliseconds, String message, Runnable runnable){
        Boolean aBoolean = syncLock(lockKeys, milliseconds, message, () -> {
            runnable.run();
            return true;
        });
        return aBoolean != null && aBoolean;
    }

    public static <T> T syncLock(LockKey lockKey, String message, Callable<T> callable){
        return syncLock(Collections.singleton(lockKey), MILLISECONDS, message, callable);
    }

    public static <T> T syncLock(Collection<LockKey> lockKeys, String message, Callable<T> callable){
        return syncLock(lockKeys, MILLISECONDS, message, callable);
    }

    public static <T> T syncLock(Collection<LockKey> lockKeys, long milliseconds, String message, Callable<T> callable){
        long current0 = System.currentTimeMillis();
        List<ISyncLock> syncLockList = lockKeys.stream().map(LockManager::getSyncLock).collect(Collectors.toList());
        if (!tryLockAll(syncLockList, milliseconds)) {
//            logger.error("lock:{} failure, message:{}", lockKeys, message, new Exception());
            return null;
        }
        try {
            long current1 = System.currentTimeMillis();
            T value = callable.call();
            long current2 = System.currentTimeMillis();
            long duration = current2 - current0;
            if (duration >= WARN_TIME){
//                logger.warn("lock:{} time:{}, call duration:{}(ms), message:{}", lockKeys, current1 - current0, duration, message, new Exception());
            }
            return value;
        }
        catch (Throwable t){
            logger.error("lock:{} exception, message:{}", lockKeys, message, t);
            return null;
        }
        finally {
            unlockAll(syncLockList);
        }
    }

    private static <T extends ISyncLock> boolean tryLockAll(List<T> syncLockList, long milliseconds) {
        syncLockList.sort(ISyncLock::compareTo);
        List<T> lockedSyncLockList = new ArrayList<>();
        RuntimeException exception = null;
        try {
            for (T syncLock : syncLockList) {
                if (syncLock.tryLock(milliseconds)) {
                    lockedSyncLockList.add(syncLock);
                    logger.trace("lock:{} lock success.", syncLock.getLockKey());
                }
                else {
                    break;
                }
            }
        } catch (RuntimeException e) {
            exception = e;
        }
        if (syncLockList.size() == lockedSyncLockList.size()){
            return true;
        }
        rollback(lockedSyncLockList);
        if (exception != null){
            throw exception;
        }
        return false;
    }

    private static <T extends ISyncLock> void rollback(List<T> syncLockList){
        unlockAll(syncLockList, true);
    }

    private static <T extends ISyncLock> void unlockAll(List<T> syncLockList){
        unlockAll(syncLockList, false);
    }

    private static <T extends ISyncLock> void unlockAll(List<T> syncLockList, boolean rollback){
        for (T lockedLock : syncLockList) {
            try {
                lockedLock.unlock();
                if (rollback){
                    logger.trace("rock:{} roll back success.", lockedLock.getLockKey());
                }
                else {
                    logger.trace("lock:{} unlock success.", lockedLock.getLockKey());
                }
            }
            catch (Throwable t) {
                if (rollback){
                    logger.error("rock:{} roll back success.", lockedLock.getLockKey(), t);
                }
                else {
                    logger.error("lock:{} unlock success.", lockedLock.getLockKey(), t);
                }
            }
        }
    }
}
