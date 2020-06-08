package com.game.cache;

import java.util.EnumMap;

/**
 * 缓存的信息的额外描述信息~
 */
public class CacheInformation {

    private final EnumMap<CacheName, Object> name2Values;

    public CacheInformation() {
        this.name2Values = new EnumMap<>(CacheName.class);
    }

    private CacheInformation(EnumMap<CacheName, Object> name2Values) {
        this.name2Values = new EnumMap<>(name2Values);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(CacheName cacheName){
        return (T)name2Values.get(cacheName);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(CacheName cacheName, T defaultValue){
        return (T)name2Values.getOrDefault(cacheName, defaultValue);
    }

    public CacheInformation copy(){
        return new CacheInformation(this.name2Values);
    }

    public boolean isEmpty(){
        return name2Values.isEmpty();
    }
}
