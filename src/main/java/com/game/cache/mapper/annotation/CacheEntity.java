package com.game.cache.mapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheEntity {

    /**
     * 主键的字段名称
     * @return
     */
    String[] primaryKeys();

    /**
     * 子键的字段名称
     * @return
     */
    String[] secondaryKeys();
}
