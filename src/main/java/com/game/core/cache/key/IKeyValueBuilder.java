package com.game.core.cache.key;

public interface IKeyValueBuilder<K> {

    /**
     * 顺序需要跟注解的Index一致
     * 第一位目前规定是Long类型的UserId
     * @param valueKey
     * @return
     */
    Object[] toKeyValue(K valueKey);

    /**
     * 转化成字符串
     * @param valueKey
     * @return
     */
    String toKeyString(K valueKey);
}
