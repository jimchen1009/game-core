package com.game.cache.mapper;

import java.lang.reflect.Field;

public class FieldInformation {

    private final int index;
    private final Field field;
    private final String annotationName;

    public FieldInformation(int index, Field field, String annotationName) {
        this.index = index;
        this.field = field;
        this.annotationName = annotationName;
    }

    public int getIndex() {
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
