package com.game.cache;

public class CollectionInfo {

    private volatile long expiredTime = 0L;
    private volatile long lastUpdateTime = 0L;

    public CollectionInfo() {
    }

    public long getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(long expiredTime) {
        this.expiredTime = expiredTime;
    }

    public boolean isExpired(long currentTime){
        return currentTime >= expiredTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
