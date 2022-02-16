package com.game.core.cache.mapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheFiled {

    /**
     * 字段的唯一ID
     * @return
     */
    int index();

    /**
     * 是否代表删除字段
     * @return
     */
    boolean isDeleted() default false;
}
