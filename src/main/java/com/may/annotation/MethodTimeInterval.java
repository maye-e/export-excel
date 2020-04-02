package com.may.annotation;

import java.lang.annotation.*;


// Target注解决定MyAnnotation注解可以加在哪些成分上，如加在类身上，或者属性身上，或者方法身上等成分
@Target(ElementType.METHOD)
// Retention注解决定MyAnnotation注解的生命周期
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodTimeInterval {
    /**
     * 注解的默认属性值
     *
     * @return 属性值
     */
    String value() default "";
}
