package com.game.cache.mapper;

import com.game.cache.InformationName;
import com.game.cache.exception.CacheException;
import com.game.cache.mapper.annotation.CacheFiled;
import com.game.cache.mapper.annotation.CacheIndex;
import com.game.cache.mapper.annotation.IndexField;
import jodd.util.StringUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ClassInformation {

    private static final Map<String, ClassInformation> name2Descriptions = new ConcurrentHashMap<>();

    public static ClassInformation get(Class<?> aClass){
        return name2Descriptions.computeIfAbsent(aClass.getName(), key-> new ClassInformation(aClass));
    }

    private final Class<?> aClass;
    private final ClassConfig classConfig;
    private List<String> primaryKeys;
    private List<String> secondaryKeys;
    private List<String> primarySecondaryKeys;
    private List<FieldInformation> descriptions;
    private List<FieldInformation> keysDescriptions;
    private List<FieldInformation> normalDescriptions;
    private final Field cacheSourceFiled;

    private ClassInformation(Class<?> aClass) {
        this.aClass = aClass;
        this.classConfig = ClassConfig.getConfig(aClass);
        this.primaryKeys = new ArrayList<>();
        this.secondaryKeys = new ArrayList<>();
        this.primarySecondaryKeys = new ArrayList<>();
        this.descriptions = new ArrayList<>();
        this.keysDescriptions = new ArrayList<>();
        this.normalDescriptions = new ArrayList<>();
        this.cacheSourceFiled = searchClassField(aClass, "isCacheResource");
        this.searchAnnotationFieldsAndInit(aClass);
    }

    public Class<?> getAClass() {
        return aClass;
    }

    public String getCacheName() {
        return aClass.getSimpleName().toLowerCase();
    }

    public List<String> getPrimaryKeys() {
        return primaryKeys;
    }

    public List<String> getSecondaryKeys() {
        return secondaryKeys;
    }

    public List<String> getPrimarySecondaryKeys() {
        return primarySecondaryKeys;
    }

    public List<FieldInformation> fieldDescriptionList() {
        return descriptions;
    }

    public List<FieldInformation> getKeysDescriptions() {
        return keysDescriptions;
    }

    public List<FieldInformation> getNormalDescriptions() {
        return normalDescriptions;
    }

    public FieldInformation findOneDescription(Predicate<FieldInformation> predicate){
        for (FieldInformation description : descriptions) {
            if (predicate.test(description)) {
                return description;
            }
        }
        return null;
    }

    public CacheIndex getCacheIndexes(){
        return aClass.getAnnotation(CacheIndex.class);
    }

    public ClassConfig getClassConfig(){
        return classConfig;
    }

    public Field getCacheSourceFiled() {
        return cacheSourceFiled;
    }

    private void searchAnnotationFieldsAndInit(Class<?> aClass){
        //字段处理~
        searchAnnotationFields(aClass, descriptions);
        Map<String, FieldInformation> name2FieldMap = new HashMap<>();
        Map<String, FieldInformation> annotationName2FieldMap = new HashMap<>();
        Set<Integer> indexes = new HashSet<>();
        for (FieldInformation description : descriptions) {
            if (name2FieldMap.put(description.getName(), description) != null) {
                throw new CacheException("multiple name:%s, class:%s", description.getName(), aClass.getName());
            }
            if (annotationName2FieldMap.put(description.getAnnotationName(), description) != null) {
                throw new CacheException("multiple annotation name:%s, class:%s", description.getAnnotationName(),aClass.getName());
            }
            if (!indexes.add(description.getIndex())){
                throw new CacheException("multiple index:%s, class:%s", description.getIndex(), aClass.getName());
            }
            if (InformationName.Names.contains(description.getAnnotationName())){
                throw new CacheException("annotation annotation name can't be %s, class:%s", description.getAnnotationName(), aClass.getName());
            }
            if (description.getIndex() > 63){
                throw new CacheException("field count is exceeds the maximum of 64, class:%s", aClass.getName());
            }
            if (primarySecondaryKeys.contains(description.getAnnotationName())){
                keysDescriptions.add(description);
            }
            else {
                normalDescriptions.add(description);
            }
        }
        this.descriptions = sortUniqueIdAndUnmodifiableList(descriptions);

        //索引处理~
        CacheIndex cacheIndex = aClass.getAnnotation(CacheIndex.class);
        this.primaryKeys = getCacheIndexNames(cacheIndex, IndexField::isPrimary, annotationName2FieldMap);
        List<String> secondaryKeys = getCacheIndexNames(cacheIndex, field -> !field.isPrimary(), annotationName2FieldMap);
        this.secondaryKeys = secondaryKeys.isEmpty() ? primaryKeys : secondaryKeys;
        this.primarySecondaryKeys = getCacheIndexNames(cacheIndex, field-> true, annotationName2FieldMap);
        Set<String> primaryAndSecondaryKeys = new HashSet<>(this.primarySecondaryKeys);
        if (primaryAndSecondaryKeys.size() != this.primarySecondaryKeys.size()){
            throw new CacheException("primarySecondaryKeys:%s, class:%s", this.primarySecondaryKeys, aClass.getName());
        }
        for (FieldInformation description : descriptions) {
            if (primaryAndSecondaryKeys.contains(description.getAnnotationName())){
                keysDescriptions.add(description);
            }
            else {
                normalDescriptions.add(description);
            }
        }
        this.keysDescriptions = sortUniqueIdAndUnmodifiableList(keysDescriptions);
        this.normalDescriptions = sortUniqueIdAndUnmodifiableList(normalDescriptions);
    }

    private List<String> getCacheIndexNames(CacheIndex cacheIndex, Predicate<IndexField> predicate, Map<String, FieldInformation> name2FieldMap){
        List<String> indexKeyList = Arrays.stream(cacheIndex.fields())
                .filter(predicate)
                .map(IndexField::name)
                .sorted(Comparator.comparingInt( name -> name2FieldMap.get(name).getIndex()))
                .collect(Collectors.toList());
        return Collections.unmodifiableList(indexKeyList);
    }

    private List<FieldInformation> sortUniqueIdAndUnmodifiableList(List<FieldInformation> descriptions){
        descriptions.sort(Comparator.comparing(FieldInformation::getIndex));
        return Collections.unmodifiableList(descriptions);
    }

    private static void searchAnnotationFields(Class<?> aClass, List<FieldInformation> descriptions) {
        if (aClass == null || aClass == Object.class) {
            return;
        }
        searchAnnotationFields(aClass.getSuperclass(), descriptions);
        for (Field field : aClass.getDeclaredFields()) {
            CacheFiled cacheFiled = field.getAnnotation(CacheFiled.class);
            if (cacheFiled == null) {
                continue;
            }
            String name = field.getName();
            field.setAccessible(true);
            String annotationName = StringUtil.isEmpty(cacheFiled.name()) ? name : cacheFiled.name();
            FieldInformation information = new FieldInformation(cacheFiled.index(), field, annotationName);
            descriptions.add(information);
        }
    }

    private static Field searchClassField(Class<?> aClass, String name) {
        while (!aClass.equals(Object.class)){
            for (Field field : aClass.getDeclaredFields()) {
                if (field.getName().equals(name)) {
                    field.setAccessible(true);
                    return field;
                }
            }
            aClass = aClass.getSuperclass();
        }
        throw new CacheException("no filed name:%s class:%s", name, aClass.getName());
    }
}
