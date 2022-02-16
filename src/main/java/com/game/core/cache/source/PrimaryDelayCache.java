package com.game.core.cache.source;

import com.game.core.cache.data.IData;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PrimaryDelayCache<K, V extends IData<K>>{

    private final long primaryKey;
    private volatile long expiredTime;
    private final Map<K, KeyDataCommand<K, V>> dataCommandMap;

    private PrimaryDelayCache(long primaryKey, long expiredTime, Map<K, KeyDataCommand<K, V>> dataCommandMap) {
        this.primaryKey = primaryKey;
        this.expiredTime = expiredTime;
        this.dataCommandMap = dataCommandMap;
    }

    public PrimaryDelayCache(long primaryKey, long expiredTime) {
        this(primaryKey, expiredTime, new ConcurrentHashMap<>());
    }

    public long getPrimaryKey() {
        return primaryKey;
    }

    public boolean isExpired(long currentTime){
        return currentTime >= expiredTime;
    }

    public long getExpiredTime() {
        return expiredTime;
    }

    public void incExpiredTime(long incTime){
        expiredTime += incTime;
    }

    public boolean isEmpty(){
        return dataCommandMap.isEmpty();
    }

    public Map<K, KeyDataCommand<K, V>> getDataCommandMap() {
        return dataCommandMap;
    }

    public Collection<K> getUpdateKeys(){
        return dataCommandMap.keySet();
    }

    public Collection<KeyDataCommand<K, V>> getAllDataCommands(){
        return dataCommandMap.values();
    }

    public void add(KeyDataCommand<K, V> updateData){
        dataCommandMap.put(updateData.getKey(), updateData);
    }

    public KeyDataCommand<K, V> get(K key){
        return dataCommandMap.get(key);
    }

    public KeyDataCommand<K, V> remove(K key){
        return dataCommandMap.remove(key);
    }

    public PrimaryDelayCache<K, V> shallowCopy(){
        return new PrimaryDelayCache<>(primaryKey, expiredTime, new ConcurrentHashMap<>(dataCommandMap));
    }
}
