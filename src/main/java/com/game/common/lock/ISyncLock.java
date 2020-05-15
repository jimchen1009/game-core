package com.game.common.lock;

public interface ISyncLock extends Comparable {

    LockKey getLockKey();

    boolean tryLock(long milliseconds);

    void unlock();
}
