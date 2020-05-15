package com.game.cache.source;

import java.util.Map;

public class KeyCacheValue<K> {

    private K key;
    private CacheCommand cacheCommand;
    private Map<String, Object> cacheValue;


    public KeyCacheValue(K key, CacheCommand cacheCommand, Map<String, Object> cacheValue) {
        this.key = key;
        this.cacheCommand = cacheCommand;
        this.cacheValue = cacheValue;
    }

    public K getKey() {
        return key;
    }

    public Map<String, Object> getCacheValue() {
        return cacheValue;
    }

    public CacheCommand getCacheCommand() {
        return cacheCommand;
    }

    public void updateCommand(CacheCommand cacheCommand){
        this.cacheCommand = cacheCommand;
    }

    public boolean isInsert(){
        return cacheCommand.equals(CacheCommand.INSERT);
    }

    public boolean isUpdate(){
        return cacheCommand.equals(CacheCommand.UPDATE);
    }

    public boolean isDeleted(){
        return cacheCommand.equals(CacheCommand.DELETE);
    }

    public static <K> KeyCacheValue<K> create(K key, boolean isCacheSource, Map<String, Object> cacheValue){
        return new KeyCacheValue<>(key, isCacheSource ? CacheCommand.UPDATE : CacheCommand.INSERT, cacheValue);
    }
}
