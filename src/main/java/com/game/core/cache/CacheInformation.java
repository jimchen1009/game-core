package com.game.core.cache;

import com.game.common.config.EvnCoreConfigs;
import com.game.common.config.EvnCoreType;

import java.util.concurrent.TimeUnit;

/**
 * 缓存的信息的额外描述信息~
 */
public class CacheInformation {

    public static CacheInformation DEFAULT = new CacheInformation();


    private volatile long expiredTime;

    public CacheInformation() {
        this(-1);
    }

    public CacheInformation(long expiredTime) {
        this.expiredTime = expiredTime;
    }

    public boolean isExpired(long currentTime){
        this.checkCurrentOrExpiredTime(currentTime);
        return expiredTime != -1 && currentTime + getOffsetDuration() >= expiredTime;
    }

    public boolean needUpdateExpired(long currentTime){
        this.checkCurrentOrExpiredTime(currentTime);
        return expiredTime != -1 && (expiredTime - getOffsetDuration() * 2) <= currentTime;
    }

    public long getExpiredTime(){
        return expiredTime;
    }

    public void updateExpiredTime(long expiredTime) {
        this.checkCurrentOrExpiredTime(expiredTime);
        if (this.expiredTime == -1 || expiredTime < this.expiredTime){
            this.expiredTime = expiredTime;
        }
    }

    public void updateCurrentTime(long currentTime){
       expiredTime = currentTime + EvnCoreConfigs.getInstance(EvnCoreType.CACHE).getDuration("redis.db.lifeDuration", TimeUnit.MILLISECONDS);
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

    private long getOffsetDuration(){
        return EvnCoreConfigs.getInstance(EvnCoreType.CACHE).getDuration("redis.db.offsetDuration", TimeUnit.MILLISECONDS);
    }
}
