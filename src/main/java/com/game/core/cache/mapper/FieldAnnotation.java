package com.game.core.cache.mapper;

import java.lang.reflect.Field;

public class FieldAnnotation {

    private final int index;
    private final Field field;
    private final String annotationName;

    public FieldAnnotation(int index, Field field, String annotationName) {
        this.index = index;
        this.field = field;
        this.annotationName = annotationName;
    }

    public int getUniqueId() {
        return index;
    }

    public Field getField() {
        return field;
    }

    public String getName() {
        return field.getName();
    }

    public String getAnnotationName() {
        return annotationName;
    }

    public Class<?> getType() {
        return field.getType();
    }
}
