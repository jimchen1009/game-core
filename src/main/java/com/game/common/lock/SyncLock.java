package com.game.common.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public abstract class SyncLock implements ISyncLock {

    private static final Logger logger = LoggerFactory.getLogger(SyncLock.class);

    private final LockKey lockKey;

    public SyncLock(LockKey lockKey) {
        this.lockKey = lockKey;
    }

    @Override
    public LockKey getLockKey() {
        return lockKey;
    }

    @Override
    public int compareTo(Object o) {
        SyncLock that = (SyncLock) o;
        int compare = Integer.compare(lockKey.hashCode(), that.lockKey.hashCode());
        if (compare == 0) {
            compare =  this.lockKey.toLockName().compareTo(that.getLockKey().toLockName());
        }
        return compare;
    }

    @Override
    public String toString() {
       return lockKey.toLockName();
    }

    public static class ReentrantSyncLock extends SyncLock{

       private final ReentrantLock reentrantLock;

        public ReentrantSyncLock(LockKey lockKey) {
            super(lockKey);
            this.reentrantLock = new ReentrantLock();
        }

        @Override
        public boolean tryLock(long milliseconds) {
            try {
                return reentrantLock.tryLock(milliseconds, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                logger.error("reentrantLock:{} failure.", getLockKey(), e);
            }
            return false;
        }

        @Override
        public void unlock() {
            reentrantLock.unlock();
        }
    }
}
