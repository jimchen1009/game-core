package com.game.cache.mapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheEntity {

    /**
     * @return
     */
    String addressName();

    /**
     *
     * @return
     */
    int primaryId();

    /**
     * 是否延迟更新
     * @return
     */
    boolean delayUpdate();
}
