package com.game.core.cache;

import com.game.common.config.EvnCoreConfigs;

import java.util.concurrent.TimeUnit;

/**
 * 缓存的信息的额外描述信息~
 */
public class CacheInformation {

    public static CacheInformation DEFAULT = new CacheInformation();

    private final static long LifeDuration = EvnCoreConfigs.getDuration("cache.redis.db.lifeDuration", TimeUnit.MILLISECONDS);

    private final static long OffsetDuration = EvnCoreConfigs.getDuration("cache.redis.db.offsetDuration", TimeUnit.MILLISECONDS);


    private volatile long expiredTime;

    public CacheInformation() {
        this(-1);
    }

    public CacheInformation(long expiredTime) {
        this.expiredTime = expiredTime;
    }

    public boolean isExpired(long currentTime){
        this.checkCurrentOrExpiredTime(currentTime);
        return !isPermanent() && currentTime + OffsetDuration >= expiredTime;
    }

    public boolean needUpdateExpired(long currentTime){
        this.checkCurrentOrExpiredTime(currentTime);
        return !isPermanent() && (expiredTime - OffsetDuration * 2) <= currentTime;
    }

    public long getExpiredTime(){
        return expiredTime;
    }

    public boolean isPermanent(){
        return expiredTime == -1;
    }

    public void updateExpiredTime(long expiredTime) {
        this.checkCurrentOrExpiredTime(expiredTime);
        if (this.expiredTime == -1 || expiredTime < this.expiredTime){
            this.expiredTime = expiredTime;
        }
    }

    public void updateCurrentTime(long currentTime){
       expiredTime = currentTime + LifeDuration;
    }

    private void checkCurrentOrExpiredTime(long currentTime){
        if (currentTime > 0){
            return;
        }
        throw new UnsupportedOperationException(String.valueOf(currentTime));
    }

    public CacheInformation cloneInformation(){
        return new CacheInformation(expiredTime);
    }
}
