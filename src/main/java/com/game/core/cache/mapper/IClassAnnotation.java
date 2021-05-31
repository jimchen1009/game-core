package com.game.core.cache.mapper;

import com.game.core.cache.mapper.annotation.CacheIndexes;

import java.util.List;

public interface IClassAnnotation {

    CacheIndexes getCacheIndexes();

    String getPrimaryKey();

    List<String> getSecondaryKeyList();

    List<String> getCombineUniqueKeyList();

    List<FieldAnnotation> getFiledAnnotationList();
}
