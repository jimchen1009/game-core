package com.game.core.cache.key;

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

        @Override
        protected Long createKey0(String[] strings) {
            return Long.parseLong(strings[0]);
        }
    };

    public static final IKeyValueBuilder<Integer> IntegerBuilder = new KeyValueBuilder<Integer>() {

        @Override
        public Object[] createValue0(Integer valueKey) {
            return new Object[]{valueKey};
        }

        @Override
        public Integer createKey0(Object[] objects) {
            return (Integer) objects[0];
        }

        @Override
        protected Integer createKey0(String[] strings) {
            return Integer.parseInt(strings[0]);
        }
    };
}
