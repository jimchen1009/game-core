package com.game.cache.mapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheClass {

    /**
     * @return
     */
    String cacheName();

    /**
     * @return
     */
    int primarySharedId();

    /**
     * 是否延迟更新
     * @return
     */
    boolean delayUpdate() default false;

    /**
     * 允许其他数据加载缓存
     * @return
     */
    boolean loadOnShared();
}
