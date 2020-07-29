package com.game.cache.data;

import java.util.function.Supplier;

public interface IData<K> extends Cloneable {

    K secondaryKey();

    boolean hasBitIndex(DataBitIndex bitIndex);

    long getBitIndexBits();

    void clearCacheBitIndex();

    Object clone(Supplier<Object> supplier);

    boolean isDeleted();

    void delete(long currentTime);
}
