package com.game.cache;

import com.game.common.config.Configs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存的信息的额外描述信息~
 */
public class CacheInformation {

    private final static long LifeDuration = Math.max(24 * 3600 * 1000L, Configs.getInstance().getDuration("cache.redis.db.lifeDuration", TimeUnit.MILLISECONDS) );

    private final static long OffsetDuration = Math.max(3600 * 1000L, Configs.getInstance().getDuration("cache.redis.db.offsetDuration", TimeUnit.MILLISECONDS) );


    private final Map<String, Object> name2Values;

    public CacheInformation(Map<String, Object> name2Values) {
        this.name2Values = name2Values;
    }

    public CacheInformation() {
        this.name2Values = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(CacheName cacheName){
        return (T)name2Values.get(cacheName.getKeyName());
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(CacheName cacheName, T defaultValue){
        return (T)name2Values.getOrDefault(cacheName.getKeyName(), defaultValue);
    }

    @SuppressWarnings("unchecked")
    public <T> T removeValue(CacheName cacheName, T defaultValue){
        Object value = name2Values.remove(cacheName.getKeyName());
        return value == null ?  defaultValue : (T)value;
    }

    public Set<Map.Entry<String, Object>> entrySet(){
        return name2Values.entrySet();
    }

    public boolean isEmpty(){
        return name2Values.isEmpty();
    }

    public boolean isExpired(long currentTime){
        long expiredTime = getExpiredTime();
        return expiredTime > 0 && (currentTime + OffsetDuration >= expiredTime);
    }

    public boolean needUpdateExpired(long currentTime){
        long expiredTime = getExpiredTime();
        return expiredTime > 0 && (expiredTime - OffsetDuration * 2) <= currentTime;
    }

    public long getExpiredTime(){
        return getValue(CacheName.ExpiredTime, 0L);
    }

    public void updateExpiredTime(long currentTime){
        name2Values.put(CacheName.ExpiredTime.getKeyName(), currentTime + LifeDuration);
    }

    @Override
    public String toString() {
        return "{" +
                "name2Values=" + name2Values +
                '}';
    }
}
