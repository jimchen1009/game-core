package com.game.cache;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

public enum InformationName {
    CACHE_KEY("k1", Boolean.class),
    EXPIRED("K10", Boolean.class),
    DELETE("k11", Boolean.class),
    ;

    private final String keyName;
    private final Class<?> aClass;

    InformationName(String keyName, Class<?> aClass) {
        this.keyName = keyName;
        this.aClass = aClass;
    }

    public String getKeyName() {
        return keyName;
    }

    public Class<?> getAClass() {
        return aClass;
    }

    public static final Collection<String> Names = Collections.unmodifiableList(Arrays.stream(InformationName.values()).map(InformationName::getKeyName).collect(Collectors.toList()));
}
