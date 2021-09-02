package com.sakana.blog.common.aop;

import java.lang.annotation.*;

//type代表可以放在类上面 method代表可以放在方法上面
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogAnnotation {

    String module() default "";

    String operator() default "";

}
