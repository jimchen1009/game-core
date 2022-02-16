package com.game.core.cache.mapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CacheIndexes {

    /**
     * 主键
     * @return
     */
    String primaryKey();

    /**
     * 联合二级键
     * @return
     */
    String[] secondaryKeys() default {};
}
