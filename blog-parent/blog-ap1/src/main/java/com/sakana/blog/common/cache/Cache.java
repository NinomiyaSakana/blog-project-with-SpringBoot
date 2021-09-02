package com.sakana.blog.common.cache;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {
    //定义切点

    long expire() default 1 * 60 * 1000;

    //缓存标识
    String name() default "";
}
