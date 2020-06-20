package com.game.cache.mapper;

import com.game.cache.CacheName;
import com.game.cache.data.Data;
import com.game.cache.data.DataBitIndex;
import com.game.cache.data.IData;
import com.game.cache.exception.CacheException;
import com.game.cache.mapper.annotation.CacheFiled;
import com.game.cache.mapper.annotation.CacheIndex;
import com.game.cache.mapper.annotation.CacheIndexes;
import com.game.cache.mapper.annotation.PrimaryIndex;
import com.game.common.log.LogUtil;
import jodd.util.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

    public static ClassInformation get(String className){
        ClassInformation information = name2Descriptions.get(className);
        if (information == null){
            Class<?> aClass;
            try {
                aClass = Class.forName(className);
            }
            catch (ClassNotFoundException e) {
                throw new CacheException("", e);
            }
            information = get(aClass);
        }
       return information;
    }

    private final Class<?> aClass;
    private String primaryKey;
    private List<String> primaryUniqueKeys;
    private List<String> secondaryKeys;
    private List<String> combineUniqueKeys;
    private List<FieldInformation> descriptions;
    private List<FieldInformation> primaryUniqueDescriptions;
    private List<FieldInformation> normalDescriptions;
    private final Method bitIndexSetter;
    private final Method bitIndexCleaner;

    private ClassInformation(Class<?> aClass) {
        this.aClass = aClass;
        this.primaryUniqueKeys = new ArrayList<>();
        this.secondaryKeys = new ArrayList<>();
        this.combineUniqueKeys = new ArrayList<>();
        this.descriptions = new ArrayList<>();
        this.primaryUniqueDescriptions = new ArrayList<>();
        this.normalDescriptions = new ArrayList<>();
        this.bitIndexSetter = searchDataClassMethod(aClass, "setBitIndex");
        this.bitIndexCleaner = searchDataClassMethod(aClass, "clearBitIndex");
        this.searchAnnotationFieldsAndInit(aClass);
    }

    public Class<?> getAClass() {
        return aClass;
    }

    public String getCacheName() {
        return aClass.getSimpleName().toLowerCase();
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public List<String> getPrimaryUniqueKeys() {
        return primaryUniqueKeys;
    }

    public List<String> getSecondaryKeys() {
        return secondaryKeys;
    }

    public List<String> getCombineUniqueKeys() {
        return combineUniqueKeys;
    }

    public List<FieldInformation> fieldDescriptionList() {
        return descriptions;
    }

    public List<FieldInformation> getPrimaryUniqueDescriptions() {
        return primaryUniqueDescriptions;
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

    public CacheIndexes getCacheIndexes(){
        return aClass.getAnnotation(CacheIndexes.class);
    }

    public void invokeSetBitIndex(IData dataValue, DataBitIndex bitIndex) {
        try {
            bitIndexSetter.invoke(dataValue, bitIndex);
        }
        catch (Throwable e) {
            throw new CacheException("bitIndex:%s, %s", e, bitIndex.getUniqueId(), LogUtil.toJSONString(dataValue));
        }
    }

    public void invokeClearBitIndex(IData dataValue, DataBitIndex bitIndex) {
        try {
            bitIndexCleaner.invoke(dataValue, bitIndex);
        }
        catch (Throwable e) {
            throw new CacheException("bitIndex:%s, %s", e, bitIndex.getUniqueId(), LogUtil.toJSONString(dataValue));
        }
    }

    private void searchAnnotationFieldsAndInit(Class<?> aClass){
        //索引处理~
        CacheIndexes cacheIndexes = aClass.getAnnotation(CacheIndexes.class);

        //字段处理~
        searchAnnotationFields(aClass, descriptions);
        Map<String, FieldInformation> informationMap = descriptions.stream().collect(Collectors.toMap(FieldInformation::getAnnotationName, information -> information));

        PrimaryIndex primaryIndex = cacheIndexes.primaryIndex();
        this.primaryKey = primaryIndex.primaryKey();
        this.primaryUniqueKeys = Collections.unmodifiableList(Arrays.stream(primaryIndex.indexes()).map(CacheIndex::name).collect(Collectors.toList()));
        this.secondaryKeys = getCacheIndexNames(cacheIndexes.secondaryIndex().indexes(), informationMap);
        List<String> combineUniqueKeys = new ArrayList<>();
        combineUniqueKeys.addAll(primaryUniqueKeys);
        combineUniqueKeys.addAll(secondaryKeys);
        if (new HashSet<>(combineUniqueKeys).size() != combineUniqueKeys.size()){
            throw new CacheException("combineUniqueKeys:%s, class:%s", combineUniqueKeys, aClass.getName());
        }
        this.combineUniqueKeys = Collections.unmodifiableList(combineUniqueKeys);

        Set<Integer> indexes = new HashSet<>();
        Set<String> fieldNames = new HashSet<>();
        Set<String> annotationNames = new HashSet<>();
        for (FieldInformation description : descriptions) {
            if (!fieldNames.add(description.getName())) {
                throw new CacheException("multiple name:%s, class:%s", description.getName(), aClass.getName());
            }
            if (!indexes.add(description.getUniqueId())){
                throw new CacheException("multiple uniqueId:%s, class:%s", description.getUniqueId(), aClass.getName());
            }
            if (!annotationNames.add(description.getAnnotationName())) {
                throw new CacheException("multiple annotation name:%s, class:%s", description.getAnnotationName(),aClass.getName());
            }
            if (CacheName.Names.contains(description.getAnnotationName())){
                throw new CacheException("annotation name can't be %s, class:%s", description.getAnnotationName(), aClass.getName());
            }
            if (description.getUniqueId() > DataBitIndex.MaximumIndex){
                throw new CacheException("field count is exceeds the maximum of %s, class:%s", DataBitIndex.MaximumIndex, aClass.getName());
            }
            if (!description.getAnnotationName().equals(primaryKey) && primaryUniqueKeys.contains(description.getAnnotationName())){
                throw new CacheException("appendKeys includes annotation name %s, class:%s", description.getAnnotationName(), aClass.getName());
            }
            if (primaryUniqueKeys.contains(description.getAnnotationName())){
                primaryUniqueDescriptions.add(description);
            }
            else {
                normalDescriptions.add(description);
            }
        }
        this.descriptions = sortUniqueIdAndUnmodifiableList(descriptions);

        for (FieldInformation description : descriptions) {
            if (primaryUniqueKeys.contains(description.getAnnotationName())){
                primaryUniqueDescriptions.add(description);
            }
            else {
                normalDescriptions.add(description);
            }
        }
        this.primaryUniqueDescriptions = sortUniqueIdAndUnmodifiableList(primaryUniqueDescriptions);
        this.normalDescriptions = sortUniqueIdAndUnmodifiableList(normalDescriptions);
    }

    private List<String> getCacheIndexNames(CacheIndex[] fields, Map<String, FieldInformation> name2FieldMap){
        List<String> indexKeyList = Arrays.stream(fields)
                .map(CacheIndex::name)
                .sorted(Comparator.comparingInt( name -> name2FieldMap.get(name).getUniqueId()))
                .collect(Collectors.toList());
        return Collections.unmodifiableList(indexKeyList);
    }

    private List<FieldInformation> sortUniqueIdAndUnmodifiableList(List<FieldInformation> descriptions){
        descriptions.sort(Comparator.comparing(FieldInformation::getUniqueId));
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

    private static Method searchDataClassMethod(Class<?> aClass, String name) {
        Class<?> findClass = aClass;
        while (!findClass.equals(Object.class)){
            if (findClass.equals(Data.class)){
                for (Method method : findClass.getDeclaredMethods()) {
                    if (method.getName().equals(name)){
                        method.setAccessible(true);
                        return method;
                    }
                }
            }
            findClass = findClass.getSuperclass();
        }
        throw new CacheException("no method name:%s class:%s", name, aClass.getName());
    }
}
