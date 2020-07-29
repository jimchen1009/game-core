package com.game.core.cache;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public enum CacheName {
    PrimaryId("k1"),
    ExpiredTime("e1"),
    DataIndexBit("d1"),
    ;

    private final String keyName;

    CacheName(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyName() {
        return keyName;
    }

    public static final Collection<String> Names = Collections.unmodifiableList(Arrays.stream(CacheName.values()).map(CacheName::getKeyName).collect(Collectors.toList()));
}
