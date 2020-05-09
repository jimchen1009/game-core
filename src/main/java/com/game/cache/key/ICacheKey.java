package com.game.cache.key;

/**
 *
 */
public interface ICacheKey {

    String[] keys();

    int getLength();

    String keyString();

    String[] keys(int length);

    ICacheKey createChild(String... keys);
}
