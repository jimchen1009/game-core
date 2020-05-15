package com.game.cache.key;

public interface IKeyValueBuilder<K> {

    /**
     * 顺序需要跟注解的Index一致
     * @param valueKey
     * @return
     */
    Object[] createValue(K valueKey);

    /**
     * 顺序需要跟注解的Index一致
     * @param valueObjects
     * @return
     */
    K createKey(Object[] valueObjects);
}
