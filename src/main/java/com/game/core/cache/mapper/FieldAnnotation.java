package com.game.core.cache.mapper;

import java.lang.reflect.Field;

public class FieldAnnotation {

    private final int index;
    private final Field field;

    public FieldAnnotation(int index, Field field) {
        this.index = index;
        this.field = field;
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

    public Class<?> getType() {
        return field.getType();
    }
}
