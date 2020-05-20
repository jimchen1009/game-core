package com.game.cache.data;

import java.util.function.Supplier;

public interface IData<K> extends Cloneable {

    K secondaryKey();

    boolean isCacheResource();

    boolean isIndexChanged(int uniqueId);

    long getIndexChangedBits();

    void clearIndexChangedBits();

    Object clone(Supplier<Object> supplier);
}
