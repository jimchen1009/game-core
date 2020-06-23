package com.game.cache.mapper;

import com.game.cache.mapper.annotation.CacheIndexes;

import java.util.List;

public interface IClassAnnotation {

    CacheIndexes getCacheIndexes();

    String getPrimaryKey();

    List<String> getPrimaryKeyList();

    List<String> getSecondaryKeyList();

    List<String> getCombineUniqueKeyList();

    List<FieldAnnotation> getFiledAnnotationList();

    List<FieldAnnotation> getPrimaryFieldAnnotationList();

    List<FieldAnnotation> getNormalFieldAnnotationList();
}
