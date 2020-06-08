package com.game.cache.mapper;

import com.game.cache.data.DataBitIndex;

import java.lang.reflect.Field;

public class FieldInformation {

    private final DataBitIndex bitIndex;
    private final Field field;
    private final String annotationName;
    private final boolean internal;

    public FieldInformation(int index, Field field, String annotationName, boolean internal) {
        this.bitIndex = DataBitIndex.getBitIndex(index);
        this.field = field;
        this.annotationName = annotationName;
        this.internal = internal;
    }

    public int getUniqueId() {
        return bitIndex.getUniqueId();
    }

    public DataBitIndex getBitIndex() {
        return bitIndex;
    }

    public boolean isInternal() {
        return internal;
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
