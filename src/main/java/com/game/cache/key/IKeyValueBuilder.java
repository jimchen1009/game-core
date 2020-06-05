package com.game.cache.key;

public interface IKeyValueBuilder<K> {

    /**
     * 顺序需要跟注解的Index一致
     * 第一位目前规定是Long类型的UserId
     * @param valueKey
     * @return
     */
    Object[] toKeyValue(K valueKey);

    /**
     * 顺序需要跟注解的Index一致
     * @param valueObjects
     * @return
     */
    K createKey(Object[] valueObjects);


    /**
     * 转化成字符串
     * @param valueKey
     * @return
     */
    String toKeyString(K valueKey);


    /**
     * 转化成字符串
     * @param objects
     * @return
     */
    String toKeyString(Object[] objects);

    /**
     * 顺序需要跟注解的Index一致
     * @param string
     * @return
     */
    K createKey(String string);

}
