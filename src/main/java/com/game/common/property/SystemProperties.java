package com.game.common.property;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 系统默认的实现：
 * 1. string.hashCode() 调用
 * 2. Properties 使用 Hashtable（为什么原生接口不止使用 ConcurrentHashMap？）
 */
public class SystemProperties {

    private final Map<SystemPropertyKey, Object> caches;

    public SystemProperties() {
        this.caches = new ConcurrentHashMap<>();
    }

    public <T> T getValue(SystemPropertyKey propertyKey, String defaultValue, Function<String, T> function){
        T cacheValue = (T)caches.get(propertyKey);
        if (cacheValue == null){
            String systemValue = System.getProperty(propertyKey.property);
            if (systemValue == null){
                cacheValue = function.apply(defaultValue);
            }
            else {
                cacheValue = function.apply(systemValue);
            }
            caches.putIfAbsent(propertyKey, cacheValue);
            cacheValue = (T)caches.get(propertyKey);
        }
        return cacheValue;
    }
}
