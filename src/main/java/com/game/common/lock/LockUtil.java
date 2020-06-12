package com.game.common.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class LockUtil {

    private static final long MILLISECONDS = 500L;
    private static final long WARN_TIME = 1000L;

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
            logger.warn("lock:{} failure, message:{}", lockKeys, message);
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
        if (lockedSyncLockList.size() == syncLockList.size()){
            return true;
        }
        rollback(lockedSyncLockList);
        if (exception != null){
            throw exception;
        }
        return true;
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
