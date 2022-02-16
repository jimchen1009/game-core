package com.game.core.cache.data;

import java.util.function.Supplier;

public interface IData<K> extends Cloneable {

    K secondaryKey();

    Object clone(Supplier<Object> supplier);

    default boolean isDeleted() { return false; }

    default boolean delete(long currentTime) { return false; }
}
