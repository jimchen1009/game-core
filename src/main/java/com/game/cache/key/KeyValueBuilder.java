package com.game.cache.key;

import com.game.cache.exception.CacheException;
import jodd.util.StringUtil;

/**
 * 作为主键的值，都是需要定义为基础类型
 * @param <K>
 */
public abstract class KeyValueBuilder<K> implements IKeyValueBuilder<K>{

    @Override
    public Object[] toKeyValue(K valueKey) {
        Object[] objects = createValue0(valueKey);
        checkOnlyPrimitiveClass(objects);
        return objects;
    }

    protected abstract Object[] createValue0(K valueKey);

    @Override
    public K createKey(Object[] objects) {
        checkOnlyPrimitiveClass(objects);
        return createKey0(objects);
    }

    protected abstract K createKey0(Object[] objects);


    @Override
    public String toKeyString(K valueKey) {
        return StringUtil.join(toKeyValue(valueKey), ".");
    }

    @Override
    public String toKeyString(Object[] objects) {
        return StringUtil.join(objects, ".");
    }

    @Override
    public K createKey(String string) {
        String[] strings = StringUtil.split(string, ".");
        return createKey0(strings);
    }

    protected abstract K createKey0(String[] strings);

    private void checkOnlyPrimitiveClass(Object[] objects){
        for (Object object : objects) {
            Class<?> aClass = object.getClass();
        if (aClass.isPrimitive()
                || aClass.equals(Long.class)
                || aClass.equals(Integer.class)
                || aClass.equals(Short.class)
                || aClass.equals(Byte.class)
                || aClass.equals(Float.class)
                || aClass.equals(Double.class)) {
            }
            else {
                throw new CacheException("don't support %s", aClass.getName());
            }
        }
    }
}
