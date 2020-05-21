package com.game.cache;

import com.game.cache.exception.CacheException;
import com.game.cache.mapper.ClassDescription;
import com.game.cache.mapper.annotation.CacheClass;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 这个类是缓存之间通信使用的~
 */
public class CacheInteraction {

    private final Map<String, ClassDescription> classes;
    private Map<String, List<CacheClass>> name2CacheClasses;

    public CacheInteraction() {
        this.classes = new HashMap<>();
        this.name2CacheClasses = new ConcurrentHashMap<>();
    }

    public synchronized void addClass(Class<?> aClass){
        if (classes.containsKey(aClass.getName())){
            return;
        }
        classes.put(aClass.getName(), ClassDescription.get(aClass));

        Map<String, List<CacheClass>> name2CacheClassList = new ConcurrentHashMap<>();
        for (ClassDescription description : classes.values()) {
            CacheClass cacheClass = description.getCacheClass();
            List<CacheClass> cacheClassList = name2CacheClassList.computeIfAbsent(cacheClass.cacheName(), key -> new ArrayList<>());
            cacheClassList.add(cacheClass);
        }
        for (Map.Entry<String, List<CacheClass>> entry : name2CacheClassList.entrySet()) {
            entry.getValue().sort(Comparator.comparingInt(CacheClass::primarySharedId));
        }
        onCheckCacheClasses(name2CacheClassList);
        this.name2CacheClasses = name2CacheClassList;
    }

    public List<CacheClass> getCacheClasses(String cacheName){
        List<CacheClass> cacheClasses = name2CacheClasses.get(cacheName);
        return cacheClasses == null ? new ArrayList<>() : new ArrayList<>(cacheClasses);
    }

    private void onCheckCacheClasses(Map<String, List<CacheClass>> name2CacheClassList){
        for (Map.Entry<String, List<CacheClass>> entry : name2CacheClassList.entrySet()) {
            List<Integer> primarySharedIds = entry.getValue().stream().map(CacheClass::primarySharedId).collect(Collectors.toList());
            for (int i = 1; i < primarySharedIds.size(); i++) {
                if (!primarySharedIds.get(i).equals(primarySharedIds.get(i - 1))){
                    continue;
                }
                else {
                    throw new CacheException("cacheName:%s primarySharedId:%s", entry.getKey(), primarySharedIds);
                }
            }
        }
    }
}
