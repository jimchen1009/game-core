package com.game.cache.key;

public interface IKeyValueBuilder<K> {

    /**
     * 根据键值创建
     * @param key
     * @return
     */
    Object[] buildValue(K key);
}
