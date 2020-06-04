package com.game.cache.dao;

import com.game.cache.exception.CacheException;
import com.game.cache.mapper.ClassInformation;
import com.game.cache.mapper.ClassConfig;
import com.game.cache.source.CacheCollection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClassesInformation {

    private final Map<String, ClassInformation> classes;
    private Map<String, List<ClassInformation>> name2CacheClasses;

    public ClassesInformation() {
        this.classes = new HashMap<>();
        this.name2CacheClasses = new HashMap<>();
    }

    public synchronized void addClass(Class<?> aClass){
        if (classes.containsKey(aClass.getName())){
            return;
        }
        classes.put(aClass.getName(), ClassInformation.get(aClass));

        Map<String, List<ClassInformation>> name2CacheClassList = new HashMap<>();
        for (ClassInformation description : classes.values()) {
            ClassConfig classConfig = description.getClassConfig();
            List<ClassInformation> cacheClassList = name2CacheClassList.computeIfAbsent(classConfig.tableName, key -> new ArrayList<>());
            cacheClassList.add(description);
        }
        for (Map.Entry<String, List<ClassInformation>> entry : name2CacheClassList.entrySet()) {
            entry.getValue().sort(Comparator.comparingInt( description -> description.getClassConfig().primarySharedId));
        }
        onCheckCacheClasses(name2CacheClassList);
        this.name2CacheClasses = name2CacheClassList;
    }

    public ClassConfig getCacheClass(Class<?> aClass){
        ClassInformation information = classes.get(aClass);
        return information == null ? null : information.getClassConfig();
    }

    public Class<?> getClass(String tableName, int primarySharedId){
        Optional<ClassInformation> optional = name2CacheClasses.get(tableName).stream()
                .filter(description -> description.getClassConfig().primarySharedId == primarySharedId)
                .findFirst();
        return optional.<Class<?>>map(ClassInformation::getAClass).orElse(null);
    }

    private void onCheckCacheClasses(Map<String, List<ClassInformation>> name2CacheClassList){
        for (Map.Entry<String, List<ClassInformation>> entry : name2CacheClassList.entrySet()) {
            List<Integer> primarySharedIds = entry.getValue().stream().map(description -> description.getClassConfig().primarySharedId).collect(Collectors.toList());
            if (primarySharedIds.size() < 2){
                continue;
            }
            if (primarySharedIds.contains(0)) {
                throw new CacheException("tableName: %s, primarySharedId list contains 0.", entry.getKey());
            }
            for (int i = 1; i < primarySharedIds.size(); i++) {
                if (primarySharedIds.get(i).equals(primarySharedIds.get(i - 1))){
                    throw new CacheException("tableName: %s, the same primarySharedId: %s.", entry.getKey(), primarySharedIds);
                }
            }
        }
    }

    public List<Integer> getPrimarySharedIds(String tableName, int primarySharedId) {
        List<Integer> primarySharedIds = name2CacheClasses.get(tableName).stream()
                .filter(description -> description.getClassConfig().loadOnShared)
                .map(description -> description.getClassConfig().primarySharedId)
                .collect(Collectors.toList());
        if (!primarySharedIds.contains(primarySharedId)) {
            primarySharedIds.add(primarySharedId);
        }
        primarySharedIds.sort(Integer::compareTo);
        return primarySharedIds;
    }

    private static final class NameCacheCollection {

        private Map<Object, Map<Integer, CacheCollection>> key2Collections;

        private NameCacheCollection() {
            this.key2Collections = new ConcurrentHashMap<>();
        }

        void addCollections(Object primaryKey, Map<Integer, CacheCollection> collections){
            Map<Integer, CacheCollection> id2Collections = key2Collections.computeIfAbsent(primaryKey, key -> new ConcurrentHashMap<>());
            id2Collections.putAll(collections);
        }

        CacheCollection removeCollection(Object primaryKey, int primarySharedId){
            Map<Integer, CacheCollection> id2Collections = key2Collections.get(primaryKey);
            if (id2Collections == null){
                return null;
            }
            CacheCollection collection = id2Collections.remove(primarySharedId);
            if (id2Collections.isEmpty()) {
                key2Collections.remove(primaryKey);
            }
            return collection;
        }
    }
}
