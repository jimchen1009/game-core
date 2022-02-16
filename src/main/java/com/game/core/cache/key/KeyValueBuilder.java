package com.game.core.cache.key;

import com.game.core.cache.exception.CacheException;
import jodd.util.StringUtil;

import java.util.Collections;
import java.util.List;

/**
 * 作为主键的值，都是需要定义为基础类型
 * @param <K>
 */
public abstract class KeyValueBuilder<K> implements IKeyValueBuilder<K>{


    @Override
    public List<Object> toKeyValue(K valueKey) {
        List<Object> objectList = createValue0(valueKey);
        checkOnlyPrimitiveClass(objectList);
        return objectList;
    }

    protected abstract List<Object> createValue0(K valueKey);

    @Override
    public String toKeyString(K valueKey) {
        return StringUtil.join(toKeyValue(valueKey), ".");
    }

    private void checkOnlyPrimitiveClass(List<Object> objectList){
        for (Object object : objectList) {
            Class<?> aClass = object.getClass();
            if (aClass.isPrimitive() || aClass.equals(Long.class)
                || aClass.equals(Integer.class)
                || aClass.equals(Short.class)
                || aClass.equals(Byte.class)
                || aClass.equals(Float.class)
                || aClass.equals(Double.class)) {
                //基础类型
            }
            else if (aClass.equals(String.class) && !((String) object).contains(".")){
                //不包括.的符号
            }
            else {
                throw new CacheException("don't support %s", aClass.getName());
            }
        }
    }


    public static final class ONE<K> extends KeyValueBuilder<K>{

        @Override
        protected List<Object> createValue0(K valueKey) {
            return Collections.singletonList(valueKey);
        }
    }
}
