package com.game.cache.mapper;

import com.game.cache.CacheName;
import com.game.cache.data.DataBitIndex;
import com.game.cache.exception.CacheException;
import com.game.cache.mapper.annotation.CacheFiled;
import com.game.cache.mapper.annotation.CacheIndex;
import com.game.cache.mapper.annotation.CacheIndexes;
import com.game.cache.mapper.annotation.PrimaryIndex;
import jodd.util.StringUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClassAnnotation implements IClassAnnotation {

    private static final Map<String, ClassAnnotation> name2Descriptions = new ConcurrentHashMap<>();

    public static ClassAnnotation create(Class<?> aClass){
        return name2Descriptions.computeIfAbsent(aClass.getName(), key-> new ClassAnnotation(aClass));
    }

    private final CacheIndexes cacheIndexes;
    private String primaryKey;
    private List<String> primaryKeyList;
    private List<String> secondaryKeyList;
    private List<String> combineUniqueKeyList;
    private List<FieldAnnotation> fieldAnnotationList;
    private List<FieldAnnotation> primaryFieldAnnotationList;
    private List<FieldAnnotation> normalFieldAnnotationList;

    private ClassAnnotation(Class<?> aClass) {
        this.cacheIndexes = aClass.getAnnotation(CacheIndexes.class);
        this.primaryKeyList = new ArrayList<>();
        this.secondaryKeyList = new ArrayList<>();
        this.combineUniqueKeyList = new ArrayList<>();
        this.fieldAnnotationList = new ArrayList<>();
        this.primaryFieldAnnotationList = new ArrayList<>();
        this.normalFieldAnnotationList = new ArrayList<>();
        this.searchAnnotationFieldsAndInit(aClass);
    }

    @Override
    public String getPrimaryKey() {
        return primaryKey;
    }

    @Override
    public List<String> getPrimaryKeyList() {
        return primaryKeyList;
    }

    @Override
    public List<String> getSecondaryKeyList() {
        return secondaryKeyList;
    }

    @Override
    public List<String> getCombineUniqueKeyList() {
        return combineUniqueKeyList;
    }

    @Override
    public List<FieldAnnotation> getFiledAnnotationList() {
        return fieldAnnotationList;
    }

    @Override
    public List<FieldAnnotation> getPrimaryFieldAnnotationList() {
        return primaryFieldAnnotationList;
    }

    @Override
    public List<FieldAnnotation> getNormalFieldAnnotationList() {
        return normalFieldAnnotationList;
    }

    @Override
    public CacheIndexes getCacheIndexes(){
        return cacheIndexes;
    }

    private void searchAnnotationFieldsAndInit(Class<?> aClass){
        //索引处理~
        CacheIndexes cacheIndexes = aClass.getAnnotation(CacheIndexes.class);

        //字段处理~
        searchAnnotationFields(aClass, fieldAnnotationList);
        Map<String, FieldAnnotation> informationMap = fieldAnnotationList.stream().collect(Collectors.toMap(FieldAnnotation::getAnnotationName, information -> information));

        PrimaryIndex primaryIndex = cacheIndexes.primaryIndex();
        this.primaryKey = primaryIndex.primaryKey();
        this.primaryKeyList = Collections.unmodifiableList(Arrays.stream(primaryIndex.indexes()).map(CacheIndex::name).collect(Collectors.toList()));
        this.secondaryKeyList = getCacheIndexNames(cacheIndexes.secondaryIndex().indexes(), informationMap);
        List<String> combineUniqueKeys = new ArrayList<>();
        combineUniqueKeys.addAll(primaryKeyList);
        combineUniqueKeys.addAll(secondaryKeyList);
        if (new HashSet<>(combineUniqueKeys).size() != combineUniqueKeys.size()){
            throw new CacheException("combineUniqueKeyList:%s, class:%s", combineUniqueKeys, aClass.getName());
        }
        this.combineUniqueKeyList = Collections.unmodifiableList(combineUniqueKeys);

        Set<Integer> indexes = new HashSet<>();
        Set<String> fieldNames = new HashSet<>();
        Set<String> annotationNames = new HashSet<>();
        for (FieldAnnotation fieldAnnotation : fieldAnnotationList) {
            if (!fieldNames.add(fieldAnnotation.getName())) {
                throw new CacheException("multiple name:%s, class:%s", fieldAnnotation.getName(), aClass.getName());
            }
            if (!indexes.add(fieldAnnotation.getUniqueId())){
                throw new CacheException("multiple uniqueId:%s, class:%s", fieldAnnotation.getUniqueId(), aClass.getName());
            }
            if (!annotationNames.add(fieldAnnotation.getAnnotationName())) {
                throw new CacheException("multiple annotation name:%s, class:%s", fieldAnnotation.getAnnotationName(),aClass.getName());
            }
            if (CacheName.Names.contains(fieldAnnotation.getAnnotationName())){
                throw new CacheException("annotation name can't be %s, class:%s", fieldAnnotation.getAnnotationName(), aClass.getName());
            }
            if (fieldAnnotation.getUniqueId() > DataBitIndex.MaximumIndex){
                throw new CacheException("field count is exceeds the maximum of %s, class:%s", DataBitIndex.MaximumIndex, aClass.getName());
            }
            if (!fieldAnnotation.getAnnotationName().equals(primaryKey) && primaryKeyList.contains(fieldAnnotation.getAnnotationName())){
                throw new CacheException("appendKeys includes annotation name %s, class:%s", fieldAnnotation.getAnnotationName(), aClass.getName());
            }
            if (primaryKeyList.contains(fieldAnnotation.getAnnotationName())){
                primaryFieldAnnotationList.add(fieldAnnotation);
            }
            else {
                normalFieldAnnotationList.add(fieldAnnotation);
            }
        }
        this.fieldAnnotationList = sortUniqueIdAndUnmodifiableList(fieldAnnotationList);

        for (FieldAnnotation description : fieldAnnotationList) {
            if (primaryKeyList.contains(description.getAnnotationName())){
                primaryFieldAnnotationList.add(description);
            }
            else {
                normalFieldAnnotationList.add(description);
            }
        }
        this.primaryFieldAnnotationList = sortUniqueIdAndUnmodifiableList(primaryFieldAnnotationList);
        this.normalFieldAnnotationList = sortUniqueIdAndUnmodifiableList(normalFieldAnnotationList);
    }

    private List<String> getCacheIndexNames(CacheIndex[] fields, Map<String, FieldAnnotation> name2FieldMap){
        List<String> indexKeyList = Arrays.stream(fields)
                .map(CacheIndex::name)
                .sorted(Comparator.comparingInt( name -> name2FieldMap.get(name).getUniqueId()))
                .collect(Collectors.toList());
        return Collections.unmodifiableList(indexKeyList);
    }

    private List<FieldAnnotation> sortUniqueIdAndUnmodifiableList(List<FieldAnnotation> descriptions){
        descriptions.sort(Comparator.comparing(FieldAnnotation::getUniqueId));
        return Collections.unmodifiableList(descriptions);
    }

    private static void searchAnnotationFields(Class<?> aClass, List<FieldAnnotation> descriptions) {
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
            FieldAnnotation information = new FieldAnnotation(cacheFiled.index(), field, annotationName);
            descriptions.add(information);
        }
    }
}
