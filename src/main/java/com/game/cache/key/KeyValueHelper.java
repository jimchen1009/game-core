package com.game.cache.key;

public class KeyValueHelper {

    private static final Object[] EMPTY = new Object[0];

    public static final IKeyValueBuilder<Long> LongBuilder = new IKeyValueBuilder<Long>() {
        
        @Override
        public Object[] createValue(Long valueKey) {
            return new Object[]{valueKey};
        }

        @Override
        public Long createKey(Object[] valueObjects) {
            return (Long) valueObjects[0];
        }
    };

    public static final IKeyValueBuilder<Integer> IntegerBuilder = new IKeyValueBuilder<Integer>() {

        @Override
        public Object[] createValue(Integer valueKey) {
            return new Object[]{valueKey};
        }

        @Override
        public Integer createKey(Object[] valueObjects) {
            return (Integer) valueObjects[0];
        }
    };

    public final static class EmptyBuilder<T> implements IKeyValueBuilder<T>{

        @Override
        public Object[] createValue(T valueKey) {
            return new Object[0];
        }

        @Override
        public T createKey(Object[] valueObjects) {
            return null;
        }
    }
}
