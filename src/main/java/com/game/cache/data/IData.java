package com.game.cache.data;

public interface IData<K> extends Cloneable {

    K secondaryKey();

    boolean isCacheResource();
}
