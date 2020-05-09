package com.game.cache.key;

import com.game.cache.exception.CacheException;
import com.game.cache.property.CachePropertyKey;
import jodd.util.ArraysUtil;
import jodd.util.StringUtil;

import java.util.Arrays;

public class CacheKey implements ICacheKey {

    public static String getKeySeparator(){
        return CachePropertyKey.KEY_SEPARATOR.getString();
    }

    public static String[] requireKeys(String... keys){
        if (keys == null || keys.length == 0){
            throw new CacheException("empty keys");
        }
        for (String key : keys) {
            requireKey(key);
        }
        return keys;
    }

    public static String requireKey(String key){
        if (StringUtil.isEmpty(key)){
            throw new CacheException("empty key");
        }
        else if (key.contains(getKeySeparator())){
            throw new CacheException("invalid key:%s", key);
        }
        else {
            return key;
        }
    }

    private String[] keys;

    public CacheKey(String... keys) {
        this.keys = requireKeys(keys);
    }

    @Override
    public String[] keys() {
        return keys;
    }

    @Override
    public int getLength() {
        return keys.length;
    }

    @Override
    public String keyString() {
        return StringUtil.join(keys, getKeySeparator());
    }

    @Override
    public String[] keys(int length) {
        if (length > getLength()){
            throw new CacheException("length:%s > maximum length:%s ", length, keyString());
        }
        else if (length == getLength()){
            return keys;
        }
        else {
            return ArraysUtil.subarray(this.keys,0, length);
        }
    }

    @Override
    public CacheKey createChild(String... keys){
        String[] strings = ArraysUtil.insert(this.keys, requireKeys(keys), 0);
        return new CacheKey(strings);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheKey cacheKey = (CacheKey) o;
        return Arrays.equals(keys, cacheKey.keys);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(keys);
    }
}
