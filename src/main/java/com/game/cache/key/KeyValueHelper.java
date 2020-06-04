package com.game.cache.key;

public class KeyValueHelper {

    private static final Object[] EMPTY = new Object[0];

    public static final IKeyValueBuilder<Long> LongBuilder = new KeyValueBuilder<Long>() {
        
        @Override
        public Object[] createValue0(Long valueKey) {
            return new Object[]{valueKey};
        }

        @Override
        public Long createKey0(Object[] objects) {
            return (Long) objects[0];
        }
    };

    public static final IKeyValueBuilder<Integer> IntegerBuilder = new KeyValueBuilder<Integer>() {

        @Override
        public Object[] createValue0(Integer valueKey) {
            return new Object[]{valueKey.intValue()};
        }

        @Override
        public Integer createKey0(Object[] objects) {
            return (Integer) objects[0];
        }
    };
}
