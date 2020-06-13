package com.game.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 缓存的信息的额外描述信息~
 */
public class CacheInformation {

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

    public void setValue(CacheName cacheName, Object defaultValue){
        name2Values.put(cacheName.getKeyName(), defaultValue);
    }

    public Set<Map.Entry<String, Object>> entrySet(){
        return name2Values.entrySet();
    }

    public boolean isEmpty(){
        return name2Values.isEmpty();
    }
}
