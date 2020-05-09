package com.game.cache.key;

public class KeyValueHelper {

    public static final IKeyValueBuilder<Long> LongBuilder = KeyValueHelper::createKeyValues;

    public static final IKeyValueBuilder<Integer> IntegerBuilder = KeyValueHelper::createKeyValues;


    private static Object[] createKeyValues(Object... objects){
        return objects;
    }
}
