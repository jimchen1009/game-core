package com.game.core.cache.mapper;

import com.game.core.cache.CacheName;
import com.game.core.cache.data.DataBitIndex;
import com.game.core.cache.exception.CacheException;
import com.game.core.cache.mapper.annotation.CacheFiled;
import com.game.core.cache.mapper.annotation.CacheIndexes;
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

public class ClassAnnotation implements IClassAnnotation {

    private static final Map<String, ClassAnnotation> name2Descriptions = new ConcurrentHashMap<>();

    public static ClassAnnotation create(Class<?> aClass){
        return name2Descriptions.computeIfAbsent(aClass.getName(), key-> new ClassAnnotation(aClass));
    }

    private String primaryKey;
    private List<String> secondaryKeyList;
    private List<String> additionalKeyList;
    private List<String> combineUniqueKeyList;
    private List<FieldAnnotation> fieldAnnotationList;

    private ClassAnnotation(Class<?> aClass) {
        this.secondaryKeyList = new ArrayList<>();
        this.combineUniqueKeyList = new ArrayList<>();
        this.fieldAnnotationList = new ArrayList<>();
        this.searchAnnotationFieldsAndInit(aClass);
    }

    @Override
    public String getPrimaryKey() {
        return primaryKey;
    }

    @Override
    public List<String> getAdditionalKeyList() {
        return additionalKeyList;
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

    private void searchAnnotationFieldsAndInit(Class<?> aClass){
        //索引处理~
        CacheIndexes cacheIndexes = aClass.getAnnotation(CacheIndexes.class);

        //字段处理~
        searchAnnotationFields(aClass, fieldAnnotationList);
        this.primaryKey = cacheIndexes.primaryKey();
        this.additionalKeyList = Arrays.asList(cacheIndexes.additionalKeys());
        this.secondaryKeyList = Arrays.asList(cacheIndexes.secondaryKeys());
        List<String> combineUniqueKeys = new ArrayList<>();
        combineUniqueKeys.add(cacheIndexes.primaryKey());
        combineUniqueKeys.addAll(additionalKeyList);
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
        }
        this.fieldAnnotationList = sortUniqueIdAndUnmodifiableList(fieldAnnotationList);
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
