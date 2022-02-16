package com.game.core.cache.source.redis;

/**
 * 缓存的信息的额外描述信息~
 */
public class RedisInformation {

    private volatile long expiredTime;
    private volatile long updateInAdvance;

    public RedisInformation() {
        this(0, 0);
    }

    public RedisInformation(long expiredTime, long updateInAdvance) {
        this.expiredTime = expiredTime;
        this.updateInAdvance = updateInAdvance;
    }

    public boolean isExpired(long currentTime){
        return expiredTime > 0 && currentTime >= expiredTime;
    }

    public boolean needUpdateExpired(long currentTime){
        return expiredTime > 0 && currentTime >= (expiredTime - updateInAdvance);
    }

    public long getExpiredTime(){
        return expiredTime;
    }

    public void updateExpiredTime(long expiredTime, long updateInAdvance) {
        this.expiredTime = expiredTime;
        this.updateInAdvance = updateInAdvance;
    }

    public RedisInformation cloneInformation(){
        return new RedisInformation(expiredTime, updateInAdvance);
    }
}
