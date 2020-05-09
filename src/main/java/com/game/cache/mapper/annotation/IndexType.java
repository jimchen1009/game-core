package com.game.cache.mapper.annotation;

public enum IndexType {
    ASC(1),
    DESC(-1),
    GEO2D("2d"),
    HASHED("hashed"),
    TEXT("text");

    private final Object type;

    IndexType(final Object o) {
        type = o;
    }

    /**
     * Returns the value as needed by the index definition document
     *
     * @return the value
     */
    public Object toIndexValue() {
        return type;
    }
}
