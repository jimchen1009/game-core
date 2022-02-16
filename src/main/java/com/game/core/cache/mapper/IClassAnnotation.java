package com.game.core.cache.mapper;

import java.util.List;

public interface IClassAnnotation {

    String getPrimaryKey();

    List<String> getSecondaryKeyList();

    List<String> getCombineUniqueKeyList();

    List<String> getOtherNameList();

    List<FieldAnnotation> getFiledAnnotationList();
}
