package com.game.cache.mapper;

import com.game.cache.data.DataBitIndex;

import java.lang.reflect.Field;

public class FieldInformation {

    private final DataBitIndex bitIndex;
    private final Field field;
    private final String annotationName;

    public FieldInformation(int index, Field field, String annotationName) {
        this.bitIndex = DataBitIndex.getBitIndex(index);
        this.field = field;
        this.annotationName = annotationName;
    }

    public int getUniqueId() {
        return bitIndex.getUniqueId();
    }

    public DataBitIndex getBitIndex() {
        return bitIndex;
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
